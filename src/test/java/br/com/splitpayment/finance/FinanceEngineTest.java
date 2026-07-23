package br.com.splitpayment.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class FinanceEngineTest {
    private final FinanceEngine engine = new FinanceEngine();

    // Given split and baseline tax payment on the same date, when calculated, then the impact is zero.
    @Test
    void returnsZeroWhenSettlementAndBaselineDueDateAreEqual() {
        TaxEvent event = event("1000.00", "2026-01-15", "2026-01-15");

        FinancialImpactResult result = engine.calculateTaxFloatImpact(
                List.of(event), annualScenario("0.365", DayCountBasis.THREE_SIXTY_FIVE), zeroCashFlow());

        assertMoney("0.0000", result.netFinancialImpact());
        assertMoney("0.0000", result.taxFloatLossCost());
        assertMoney("0.0000", result.taxPaymentPostponementBenefit());
    }

    // Given due date after settlement and a positive rate, when calculated, then the float loss is positive.
    @Test
    void returnsPositiveLossWhenSplitOccursBeforeBaselineTaxDueDate() {
        TaxEvent event = event("1000.00", "2026-01-01", "2026-01-31");

        FinancialImpactResult result = engine.calculateTaxFloatImpact(
                List.of(event), annualScenario("0.365", DayCountBasis.THREE_SIXTY_FIVE), zeroCashFlow());

        assertEquals(1, result.taxFloatLossCost().compareTo(BigDecimal.ZERO));
        assertMoney(result.taxFloatLossCost().toPlainString(), result.netFinancialImpact());
        assertMoney("0.0000", result.taxPaymentPostponementBenefit());
    }

    // Given due date before settlement and a positive rate, when calculated, then it is a postponement benefit.
    @Test
    void returnsPostponementBenefitWhenSplitOccursAfterBaselineTaxDueDate() {
        TaxEvent event = event("1000.00", "2026-01-31", "2026-01-01");

        FinancialImpactResult result = engine.calculateTaxFloatImpact(
                List.of(event), annualScenario("0.365", DayCountBasis.THREE_SIXTY_FIVE), zeroCashFlow());

        assertEquals(1, result.taxPaymentPostponementBenefit().compareTo(BigDecimal.ZERO));
        assertEquals(-1, result.netFinancialImpact().signum());
        assertMoney("0.0000", result.taxFloatLossCost());
    }

    // Given split happens one full day-count period after the baseline due date,
    // when a 12% effective annual rate is applied, then the postponement benefit is 12% of the withheld amount.
    @Test
    void calculatesExactPostponementBenefitForOneFullYearOnConfiguredBasis() {
        TaxEvent event = event("1000.00", "2027-01-01", "2026-01-01");

        FinancialImpactResult result = engine.calculateTaxFloatImpact(
                List.of(event), annualScenario("0.12", DayCountBasis.THREE_SIXTY_FIVE), zeroCashFlow());

        assertMoney("-120.0000", result.netFinancialImpact());
        assertMoney("0.0000", result.taxFloatLossCost());
        assertMoney("120.0000", result.taxPaymentPostponementBenefit());
    }

    // Given no withheld amount, when calculated, then dates and rates cannot generate financial impact.
    @Test
    void returnsZeroWhenWithheldAmountIsZero() {
        TaxEvent event = event("0.00", "2026-01-01", "2026-12-31");

        FinancialImpactResult result = engine.calculateTaxFloatImpact(
                List.of(event), annualScenario("0.365", DayCountBasis.THREE_SIXTY_FIVE), zeroCashFlow());

        assertMoney("0.0000", result.netFinancialImpact());
        assertMoney("0.0000", result.taxFloatLossCost());
        assertMoney("0.0000", result.taxPaymentPostponementBenefit());
    }

    // Given a zero funding rate, when calculated, then the impact is zero regardless of elapsed days.
    @Test
    void returnsZeroWhenFundingRateIsZero() {
        TaxEvent event = event("1000.00", "2026-01-01", "2026-12-31");

        FinancialImpactResult result = engine.calculateTaxFloatImpact(
                List.of(event), annualScenario("0.00", DayCountBasis.THREE_SIXTY_FIVE), zeroCashFlow());

        assertMoney("0.0000", result.netFinancialImpact());
        assertMoney("0.0000", result.taxFloatLossCost());
        assertMoney("0.0000", result.taxPaymentPostponementBenefit());
    }

    // Given 30, 360 and 365 calendar-day intervals, when each uses its respective day-count basis,
    // then equivalent annual compounding yields the same factor after one full configured period.
    @Test
    void respectsDayCountBasisWhenConvertingEffectiveAnnualRateToDailyRate() {
        FinancialImpactResult thirty = engine.calculateTaxFloatImpact(
                List.of(event("1000.00", "2026-01-01", "2026-01-31")),
                annualScenario("0.12", DayCountBasis.THIRTY), zeroCashFlow());
        FinancialImpactResult threeSixty = engine.calculateTaxFloatImpact(
                List.of(event("1000.00", "2026-01-01", "2026-12-27")),
                annualScenario("0.12", DayCountBasis.THREE_SIXTY), zeroCashFlow());
        FinancialImpactResult threeSixtyFive = engine.calculateTaxFloatImpact(
                List.of(event("1000.00", "2026-01-01", "2027-01-01")),
                annualScenario("0.12", DayCountBasis.THREE_SIXTY_FIVE), zeroCashFlow());

        assertMoney("120.0000", thirty.taxFloatLossCost());
        assertMoney("120.0000", threeSixty.taxFloatLossCost());
        assertMoney("120.0000", threeSixtyFive.taxFloatLossCost());
    }

    // Given raw operating cash data, when the engine calculates coverage,
    // then ICOF operational uses revenue less variable cost and tax float loss over fixed cost.
    @Test
    void calculatesOperationalFixedCoverageFromRawCashVariables() {
        FinancialImpactResult result = engine.calculateTaxFloatImpact(
                List.of(event("100.00", "2026-01-01", "2027-01-01")),
                annualScenario("0.12", DayCountBasis.THREE_SIXTY_FIVE),
                cashFlow("1000.00", "400.00", "200.00")
        );

        assertMoney("1.9700", result.operationalFixedObligationCoverageIndex());
    }

    // Given initial cash and minimum reserve, when the engine calculates coverage,
    // then ICOF liquidity includes both values in the numerator.
    @Test
    void calculatesLiquidityFixedCoverageFromRawCashVariables() {
        FinancialImpactResult result = engine.calculateTaxFloatImpact(
                List.of(event("100.00", "2026-01-01", "2027-01-01")),
                annualScenario("0.12", DayCountBasis.THREE_SIXTY_FIVE, "200.00", "100.00"),
                cashFlow("1000.00", "400.00", "200.00")
        );

        assertMoney("2.2200", result.liquidityFixedObligationCoverageIndex());
    }

    // Given fixed costs equal zero, when coverage is requested,
    // then the engine raises a controlled domain exception instead of dividing by zero.
    @Test
    void rejectsCoverageCalculationWhenFixedCostsAreZero() {
        assertThrows(FixedCostCoverageNotApplicableException.class, () ->
                engine.calculateTaxFloatImpact(
                        List.of(event("100.00", "2026-01-01", "2027-01-01")),
                        annualScenario("0.12", DayCountBasis.THREE_SIXTY_FIVE),
                        cashFlow("1000.00", "0.00", "200.00")
                )
        );
    }

    // Given two tax events and two cash-flow operations, when the engine aggregates them,
    // then tax float cost and ICOF metrics are calculated once from the exact batch totals.
    @Test
    void aggregatesFloatImpactAndCashVariablesBeforeCalculatingCoverage() {
        AggregatedFinancialResult result = engine.calculateAggregatedTaxFloatImpact(
                List.of(
                        event("280.00", "2026-01-01", "2027-01-01"),
                        event("40.00", "2026-01-01", "2027-01-01")
                ),
                annualScenario("0.12", DayCountBasis.THREE_SIXTY_FIVE, "1000.00", "100.00"),
                List.of(
                        cashFlow("2000.00", "1000.00", "500.00"),
                        cashFlow("1000.00", "500.00", "200.00")
                )
        );

        assertMoney("3000.0000", result.totalNetRevenues());
        assertMoney("1500.0000", result.totalFixedCostCashOutflow());
        assertMoney("700.0000", result.totalVariableCostCashOutflow());
        assertMoney("38.4000", result.taxFloatLossCost());
        assertMoney("1.5077", result.operationalFixedObligationCoverageIndex());
        assertMoney("2.1077", result.liquidityFixedObligationCoverageIndex());
    }

    // Given future cash inflow between split withholding and baseline tax due date,
    // when both ledgers are projected by date, then Split identifies the temporary cash rupture.
    @Test
    void projectsMinimumCashBalanceSeparatelyForBaselineAndSplit() {
        AggregatedFinancialResult result = engine.calculateAggregatedTaxFloatImpact(
                List.of(
                        event("2800.00", "2026-01-01", "2026-01-03"),
                        event("0.00", "2026-01-02", "2026-01-02")
                ),
                annualScenario("0.00", DayCountBasis.THREE_SIXTY_FIVE, "100.00", "100.00"),
                List.of(
                        cashFlow("2000.00", "500.00", "0.00"),
                        cashFlow("5000.00", "0.00", "0.00")
                )
        );

        assertMoney("100.0000", result.minimumBaselineProjectedCashBalance());
        assertMoney("-1200.0000", result.minimumSplitProjectedCashBalance());
    }

    // Given a positive cash gap, adjustable revenue and positive kappa,
    // when adjustment is calculated, then the exact price-adjustment decimal is returned.
    @Test
    void calculatesEstimatedPriceAdjustmentForPositiveGapAndPositiveKappa() {
        AggregatedFinancialResult result = engine.calculateAggregatedTaxFloatImpact(
                List.of(event("2800.00", "2026-01-01", "2026-01-03")),
                pricingScenario("100.00", "100.00", "0.10", "0.05", "0.05"),
                List.of(cashFlow("2000.00", "500.00", "0.00", "3000.00"))
        );

        assertMoney("1300.0000", result.cashGap());
        assertMoney("0.5417", result.estimatedPriceAdjustmentPercentage());
    }

    // Given minimum Split cash already above the reserve, when kappa is not viable,
    // then no adjustment is required and the calculation returns zero without failing.
    @Test
    void returnsZeroAdjustmentWhenCashGapIsZero() {
        AggregatedFinancialResult result = engine.calculateAggregatedTaxFloatImpact(
                List.of(event("0.00", "2026-01-01", "2026-01-01")),
                pricingScenario("1000.00", "100.00", "1.00", "1.00", "1.00"),
                List.of(cashFlow("0.00", "1.00", "0.00", "0.00"))
        );

        assertMoney("0.0000", result.cashGap());
        assertMoney("0.0000", result.estimatedPriceAdjustmentPercentage());
    }

    // Given a positive cash gap and kappa less than or equal to zero,
    // when adjustment is calculated, then the pricing model is rejected as unviable.
    @Test
    void rejectsAdjustmentWhenKappaIsZeroOrNegative() {
        assertThrows(UnviablePricingModelException.class, () ->
                engine.calculateAggregatedTaxFloatImpact(
                        List.of(event("2800.00", "2026-01-01", "2026-01-03")),
                        pricingScenario("100.00", "100.00", "0.50", "0.30", "0.20"),
                        List.of(cashFlow("2000.00", "500.00", "0.00", "3000.00"))
                )
        );
    }

    // Given a positive cash gap and no adjustable revenue,
    // when adjustment is calculated, then the engine rejects the non-calculable result.
    @Test
    void rejectsAdjustmentWhenAdjustableRevenueIsZero() {
        assertThrows(AdjustmentNotCalculableException.class, () ->
                engine.calculateAggregatedTaxFloatImpact(
                        List.of(event("2800.00", "2026-01-01", "2026-01-03")),
                        pricingScenario("100.00", "100.00", "0.10", "0.05", "0.05"),
                        List.of(cashFlow("2000.00", "500.00", "0.00", "0.00"))
                )
        );
    }

    private static TaxEvent event(String amount, String settlementDate, String baselineTaxDueDate) {
        return new TaxEvent(
                "operation-1",
                new BigDecimal(amount),
                LocalDate.parse(settlementDate),
                LocalDate.parse(baselineTaxDueDate)
        );
    }

    private static FinancialScenario annualScenario(String annualRate, DayCountBasis basis) {
        return annualScenario(annualRate, basis, "0.00", "0.00");
    }

    private static FinancialScenario annualScenario(
            String annualRate,
            DayCountBasis basis,
            String initialAvailableCash,
            String minimumCashReserve
    ) {
        return FinancialScenario.effectiveAnnualRate(
                new BigDecimal(annualRate),
                basis,
                new BigDecimal(initialAvailableCash),
                new BigDecimal(minimumCashReserve),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    private static FinancialScenario pricingScenario(
            String initialAvailableCash,
            String minimumCashReserve,
            String variableCostMargin,
            String paymentFeeMargin,
            String commissionMargin
    ) {
        return FinancialScenario.effectiveAnnualRate(
                BigDecimal.ZERO,
                DayCountBasis.THREE_SIXTY_FIVE,
                new BigDecimal(initialAvailableCash),
                new BigDecimal(minimumCashReserve),
                new BigDecimal(variableCostMargin),
                new BigDecimal(paymentFeeMargin),
                new BigDecimal(commissionMargin)
        );
    }

    private static CashFlowOperation zeroCashFlow() {
        return cashFlow("0.00", "1.00", "0.00", "1.00");
    }

    private static CashFlowOperation cashFlow(String netRevenues, String fixedCosts, String variableCosts) {
        return cashFlow(netRevenues, fixedCosts, variableCosts, netRevenues);
    }

    private static CashFlowOperation cashFlow(
            String netRevenues,
            String fixedCosts,
            String variableCosts,
            String adjustableRevenue
    ) {
        return new CashFlowOperation(
                new BigDecimal(netRevenues),
                new BigDecimal(fixedCosts),
                new BigDecimal(variableCosts),
                new BigDecimal(adjustableRevenue)
        );
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "Expected " + expected + " but was " + actual);
    }
}
