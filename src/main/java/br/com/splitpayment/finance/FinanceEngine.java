package br.com.splitpayment.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Stateless pure Domain Service. */
public final class FinanceEngine {
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public FinancialImpactResult calculateTaxFloatImpact(
            List<TaxEvent> taxEvents,
            FinancialScenario scenario,
            CashFlowOperation cashFlowOperation
    ) {
        AggregatedFinancialResult aggregate = calculateAggregatedTaxFloatImpact(
                taxEvents,
                scenario,
                List.of(cashFlowOperation)
        );

        return new FinancialImpactResult(
                aggregate.netFinancialImpact(),
                aggregate.taxFloatLossCost(),
                aggregate.taxPaymentPostponementBenefit(),
                aggregate.operationalFixedObligationCoverageIndex(),
                aggregate.liquidityFixedObligationCoverageIndex()
        );
    }

    public AggregatedFinancialResult calculateAggregatedTaxFloatImpact(
            List<TaxEvent> taxEvents,
            FinancialScenario scenario,
            List<CashFlowOperation> cashFlowOperations
    ) {
        Objects.requireNonNull(taxEvents, "taxEvents is required.");
        Objects.requireNonNull(scenario, "scenario is required.");
        Objects.requireNonNull(cashFlowOperations, "cashFlowOperations is required.");
        if (taxEvents.isEmpty() || cashFlowOperations.isEmpty()) {
            throw new InvalidFinancialScenarioException("Tax events and cash-flow operations must not be empty.");
        }
        if (taxEvents.size() != cashFlowOperations.size()) {
            throw new InvalidFinancialScenarioException("Tax events and cash-flow operations must have the same size.");
        }

        BigDecimal totalNetRevenues = cashFlowOperations.stream()
                .map(CashFlowOperation::netRevenues)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal totalFixedCosts = cashFlowOperations.stream()
                .map(CashFlowOperation::fixedCostCashOutflow)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal totalVariableCosts = cashFlowOperations.stream()
                .map(CashFlowOperation::variableCostCashOutflow)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal totalAdjustableRevenue = cashFlowOperations.stream()
                .map(CashFlowOperation::revenueAdjustableAmount)
                .reduce(ZERO, BigDecimal::add);

        if (totalFixedCosts.signum() == 0) {
            throw new FixedCostCoverageNotApplicableException();
        }

        BigDecimal dailyRate = dailyEffectiveRate(scenario);
        List<EventImpact> eventImpacts = taxEvents.stream()
                .map(event -> calculateEventImpact(event, dailyRate, scenario))
                .toList();
        BigDecimal lossCost = eventImpacts.stream()
                .map(EventImpact::lossCost)
                .reduce(ZERO, (total, value) -> total.add(value, scenario.calculationContext()));
        BigDecimal postponementBenefit = eventImpacts.stream()
                .map(EventImpact::postponementBenefit)
                .reduce(ZERO, (total, value) -> total.add(value, scenario.calculationContext()));

        BigDecimal netImpact = lossCost.subtract(postponementBenefit, scenario.calculationContext());
        BigDecimal operationalCoverage = calculateOperationalCoverage(
                lossCost, totalNetRevenues, totalVariableCosts, totalFixedCosts, scenario);
        BigDecimal liquidityCoverage = calculateLiquidityCoverage(
                lossCost, totalNetRevenues, totalVariableCosts, totalFixedCosts, scenario);
        BigDecimal minimumBaselineCashBalance = projectMinimumCashBalance(
                taxEvents, cashFlowOperations, scenario.initialAvailableCash(), false, scenario);
        BigDecimal minimumSplitCashBalance = projectMinimumCashBalance(
                taxEvents, cashFlowOperations, scenario.initialAvailableCash(), true, scenario);
        BigDecimal cashGap = calculateCashGap(minimumSplitCashBalance, scenario);
        BigDecimal estimatedPriceAdjustment = calculateEstimatedPriceAdjustment(
                cashGap,
                totalAdjustableRevenue,
                scenario
        );
        return new AggregatedFinancialResult(
                round(totalNetRevenues, scenario),
                round(totalFixedCosts, scenario),
                round(totalVariableCosts, scenario),
                round(netImpact, scenario),
                round(lossCost, scenario),
                round(postponementBenefit, scenario),
                operationalCoverage,
                liquidityCoverage,
                round(minimumBaselineCashBalance, scenario),
                round(minimumSplitCashBalance, scenario),
                round(cashGap, scenario),
                estimatedPriceAdjustment
        );
    }

    private BigDecimal calculateCashGap(
            BigDecimal minimumSplitCashBalance,
            FinancialScenario scenario
    ) {
        BigDecimal gap = scenario.minimumCashReserve()
                .subtract(minimumSplitCashBalance, scenario.calculationContext());
        return gap.signum() > 0 ? gap : ZERO;
    }

    private BigDecimal calculateEstimatedPriceAdjustment(
            BigDecimal cashGap,
            BigDecimal totalAdjustableRevenue,
            FinancialScenario scenario
    ) {
        if (cashGap.signum() == 0) {
            return ZERO.setScale(scenario.outputScale(), scenario.outputRoundingMode());
        }
        if (totalAdjustableRevenue.signum() == 0) {
            throw new AdjustmentNotCalculableException();
        }

        BigDecimal kappa = ONE
                .subtract(scenario.incrementalVariableCostPercentage(), scenario.calculationContext())
                .subtract(scenario.incrementalPaymentFeePercentage(), scenario.calculationContext())
                .subtract(scenario.incrementalCommissionPercentage(), scenario.calculationContext());
        if (kappa.signum() <= 0) {
            throw new UnviablePricingModelException();
        }

        BigDecimal incrementalCashContribution = totalAdjustableRevenue.multiply(
                kappa,
                scenario.calculationContext()
        );
        return cashGap.divide(
                incrementalCashContribution,
                scenario.outputScale(),
                scenario.outputRoundingMode()
        );
    }

    /**
     * Costs and net revenues are recognized on settlementDate because the current contract has no separate due dates.
     * Baseline pays tax on baselineTaxDueDate; Split withholds the same amount on settlementDate.
     */
    private BigDecimal projectMinimumCashBalance(
            List<TaxEvent> taxEvents,
            List<CashFlowOperation> cashFlowOperations,
            BigDecimal initialAvailableCash,
            boolean splitScenario,
            FinancialScenario scenario
    ) {
        Map<LocalDate, BigDecimal> eventsByDate = new TreeMap<>();

        for (int index = 0; index < taxEvents.size(); index++) {
            TaxEvent taxEvent = taxEvents.get(index);
            CashFlowOperation operation = cashFlowOperations.get(index);
            BigDecimal operatingDelta = operation.netRevenues()
                    .subtract(operation.variableCostCashOutflow(), scenario.calculationContext())
                    .subtract(operation.fixedCostCashOutflow(), scenario.calculationContext());

            addDailyEvent(eventsByDate, taxEvent.settlementDate(), operatingDelta, scenario);
            LocalDate taxOutflowDate = splitScenario
                    ? taxEvent.settlementDate()
                    : taxEvent.baselineTaxDueDate();
            addDailyEvent(
                    eventsByDate,
                    taxOutflowDate,
                    taxEvent.simulatedSplitWithheldAmount().negate(),
                    scenario
            );
        }

        BigDecimal projectedBalance = initialAvailableCash;
        BigDecimal minimumBalance = initialAvailableCash;
        for (BigDecimal dailyDelta : eventsByDate.values()) {
            projectedBalance = projectedBalance.add(dailyDelta, scenario.calculationContext());
            if (projectedBalance.compareTo(minimumBalance) < 0) {
                minimumBalance = projectedBalance;
            }
        }
        return minimumBalance;
    }

    private void addDailyEvent(
            Map<LocalDate, BigDecimal> eventsByDate,
            LocalDate date,
            BigDecimal amount,
            FinancialScenario scenario
    ) {
        eventsByDate.merge(
                date,
                amount,
                (current, increment) -> current.add(increment, scenario.calculationContext())
        );
    }

    private EventImpact calculateEventImpact(
            TaxEvent event,
            BigDecimal dailyRate,
            FinancialScenario scenario
    ) {
        long signedDays = ChronoUnit.DAYS.between(event.settlementDate(), event.baselineTaxDueDate());
        BigDecimal eventImpact = calculateEventImpact(
                event.simulatedSplitWithheldAmount(), dailyRate, signedDays, scenario);
        return eventImpact.signum() >= 0
                ? new EventImpact(eventImpact, ZERO)
                : new EventImpact(ZERO, eventImpact.abs());
    }

    private BigDecimal calculateOperationalCoverage(
            BigDecimal taxFloatLossCost,
            BigDecimal totalNetRevenues,
            BigDecimal totalVariableCosts,
            BigDecimal totalFixedCosts,
            FinancialScenario scenario
    ) {
        BigDecimal numerator = totalNetRevenues
                .subtract(totalVariableCosts, scenario.calculationContext())
                .subtract(taxFloatLossCost, scenario.calculationContext());
        return divideAndRound(numerator, totalFixedCosts, scenario);
    }

    private BigDecimal calculateLiquidityCoverage(
            BigDecimal taxFloatLossCost,
            BigDecimal totalNetRevenues,
            BigDecimal totalVariableCosts,
            BigDecimal totalFixedCosts,
            FinancialScenario scenario
    ) {
        BigDecimal numerator = scenario.initialAvailableCash()
                .add(totalNetRevenues, scenario.calculationContext())
                .subtract(totalVariableCosts, scenario.calculationContext())
                .subtract(taxFloatLossCost, scenario.calculationContext())
                .subtract(scenario.minimumCashReserve(), scenario.calculationContext());
        return divideAndRound(numerator, totalFixedCosts, scenario);
    }

    private BigDecimal divideAndRound(
            BigDecimal numerator,
            BigDecimal denominator,
            FinancialScenario scenario
    ) {
        return numerator.divide(
                denominator,
                scenario.outputScale(),
                scenario.outputRoundingMode()
        );
    }

    private BigDecimal calculateEventImpact(
            BigDecimal withheldAmount,
            BigDecimal dailyRate,
            long signedDays,
            FinancialScenario scenario
    ) {
        if (withheldAmount.signum() == 0 || dailyRate.signum() == 0 || signedDays == 0) {
            return ZERO;
        }

        long absoluteDays = Math.abs(signedDays);
        BigDecimal growth = ONE.add(dailyRate, scenario.calculationContext())
                .pow(Math.toIntExact(absoluteDays), scenario.calculationContext());
        BigDecimal timeValue = withheldAmount.multiply(growth.subtract(ONE, scenario.calculationContext()), scenario.calculationContext());

        return signedDays > 0 ? timeValue : timeValue.negate();
    }

    private BigDecimal dailyEffectiveRate(FinancialScenario scenario) {
        if (scenario.fundingRate().signum() == 0) {
            return ZERO;
        }
        return switch (scenario.fundingRatePeriod()) {
            case DAY -> scenario.fundingRate();
            case MONTH -> BigDecimalMath.nthRoot(
                    ONE.add(scenario.fundingRate(), scenario.calculationContext()),
                    30,
                    scenario.calculationContext()
            ).subtract(ONE, scenario.calculationContext());
            case YEAR -> BigDecimalMath.nthRoot(
                    ONE.add(scenario.fundingRate(), scenario.calculationContext()),
                    scenario.dayCountBasis().days(),
                    scenario.calculationContext()
            ).subtract(ONE, scenario.calculationContext());
        };
    }

    private BigDecimal round(BigDecimal amount, FinancialScenario scenario) {
        return amount.setScale(scenario.outputScale(), scenario.outputRoundingMode());
    }

    private record EventImpact(BigDecimal lossCost, BigDecimal postponementBenefit) {
    }
}
