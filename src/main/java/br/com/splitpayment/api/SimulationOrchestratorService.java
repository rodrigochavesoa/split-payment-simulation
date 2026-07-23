package br.com.splitpayment.api;

import br.com.splitpayment.decision.DecisionEngine;
import br.com.splitpayment.decision.DecisionInput;
import br.com.splitpayment.decision.DecisionResult;
import br.com.splitpayment.finance.DayCountBasis;
import br.com.splitpayment.finance.AggregatedFinancialResult;
import br.com.splitpayment.finance.CashFlowOperation;
import br.com.splitpayment.finance.FinanceEngine;
import br.com.splitpayment.finance.FinancialScenario;
import br.com.splitpayment.finance.RatePeriod;
import br.com.splitpayment.tax.TaxCalculationInput;
import br.com.splitpayment.tax.AggregatedTaxResult;
import br.com.splitpayment.tax.TaxEngine;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Service;

/** ACL: maps validated HTTP DTOs to pure domain records and back to transport DTOs. */
@Service
public class SimulationOrchestratorService {
    private static final int OUTPUT_SCALE = 4;

    private final TaxEngine taxEngine;
    private final FinanceEngine financeEngine;
    private final DecisionEngine decisionEngine;

    public SimulationOrchestratorService(
            TaxEngine taxEngine,
            FinanceEngine financeEngine,
            DecisionEngine decisionEngine
    ) {
        this.taxEngine = taxEngine;
        this.financeEngine = financeEngine;
        this.decisionEngine = decisionEngine;
    }

    public SimulationResponse simulate(SimulationRequest request) {
        parseDate(request.referenceDate(), "referenceDate");
        FinancialScenario financialScenario = toFinancialScenario(request.financialScenario());
        List<TaxCalculationInput> taxInputs = request.operations().stream()
                .map(this::toTaxInput)
                .toList();
        List<CashFlowOperation> cashFlowOperations = request.operations().stream()
                .map(this::toCashFlowOperation)
                .toList();

        AggregatedTaxResult taxResult = taxEngine.calculateAll(taxInputs);
        AggregatedFinancialResult financialResult = financeEngine.calculateAggregatedTaxFloatImpact(
                taxResult.taxEvents(),
                financialScenario,
                cashFlowOperations
        );

        DecisionResult decisionResult = decisionEngine.evaluate(new DecisionInput(
                financialResult.liquidityFixedObligationCoverageIndex(),
                financialResult.minimumSplitProjectedCashBalance(),
                financialScenario.minimumCashReserve()
        ));

        return new SimulationResponse(
                request.scenarioId(),
                request.referenceDate(),
                new SimulationAuditDto(
                        request.rulesetVersion(),
                        request.operations().stream()
                                .map(operation -> new OperationAuditDto(
                                        operation.operationId(),
                                        operation.taxPolicyReference()
                                ))
                                .toList()
                ),
                new TaxResultDto(
                        format(taxResult.totalGrossTaxDebit()),
                        format(taxResult.totalSplitEligibleAmount()),
                        format(taxResult.totalSimulatedSplitWithheldAmount())
                ),
                new FinancialImpactDto(
                        format(financialResult.totalNetRevenues()),
                        format(financialResult.totalFixedCostCashOutflow()),
                        format(financialResult.totalVariableCostCashOutflow()),
                        format(financialResult.netFinancialImpact()),
                        format(financialResult.taxFloatLossCost()),
                        format(financialResult.taxPaymentPostponementBenefit()),
                        format(financialResult.operationalFixedObligationCoverageIndex()),
                        format(financialResult.liquidityFixedObligationCoverageIndex()),
                        format(financialResult.minimumBaselineProjectedCashBalance()),
                        format(financialResult.minimumSplitProjectedCashBalance()),
                        format(financialResult.cashGap()),
                        format(financialResult.estimatedPriceAdjustmentPercentage())
                ),
                new DecisionResultDto(
                        decisionResult.readinessStatus().name(),
                        decisionResult.riskLevel().name(),
                        decisionResult.analyticalMessage()
                )
        );
    }

    private TaxCalculationInput toTaxInput(OperationDto operation) {
        return new TaxCalculationInput(
                operation.operationId(),
                parseDecimal(operation.taxableBase(), "operation.taxableBase"),
                parseDecimal(operation.effectiveTaxRate(), "operation.effectiveTaxRate"),
                parseDecimal(operation.alreadyExtinguishedTaxAmount(), "operation.alreadyExtinguishedTaxAmount"),
                parseDecimal(operation.splitEligiblePercentage(), "operation.splitEligiblePercentage"),
                parseDate(operation.settlementDate(), "operation.settlementDate"),
                parseDate(operation.baselineTaxDueDate(), "operation.baselineTaxDueDate")
        );
    }

    private FinancialScenario toFinancialScenario(FinancialScenarioDto financial) {
        return new FinancialScenario(
                parseDecimal(financial.fundingRate(), "financialScenario.fundingRate"),
                parseRatePeriod(financial.fundingRatePeriod()),
                parseDayCountBasis(financial.dayCountBasis()),
                parseDecimal(financial.initialAvailableCash(), "financialScenario.initialAvailableCash"),
                parseDecimal(financial.minimumCashReserve(), "financialScenario.minimumCashReserve"),
                parseDecimal(financial.incrementalVariableCostPercentage(), "financialScenario.incrementalVariableCostPercentage"),
                parseDecimal(financial.incrementalPaymentFeePercentage(), "financialScenario.incrementalPaymentFeePercentage"),
                parseDecimal(financial.incrementalCommissionPercentage(), "financialScenario.incrementalCommissionPercentage"),
                MathContext.DECIMAL128,
                OUTPUT_SCALE,
                RoundingMode.HALF_EVEN
        );
    }

    private CashFlowOperation toCashFlowOperation(OperationDto operation) {
        return new CashFlowOperation(
                parseDecimal(operation.netRevenues(), "operation.netRevenues"),
                parseDecimal(operation.fixedCostCashOutflow(), "operation.fixedCostCashOutflow"),
                parseDecimal(operation.variableCostCashOutflow(), "operation.variableCostCashOutflow"),
                parseDecimal(operation.revenueAdjustableAmount(), "operation.revenueAdjustableAmount")
        );
    }

    private BigDecimal parseDecimal(String rawValue, String field) {
        try {
            return new BigDecimal(rawValue);
        } catch (NumberFormatException exception) {
            throw new InvalidPayloadException(field, "must be a valid decimal string.");
        }
    }

    private LocalDate parseDate(String rawValue, String field) {
        try {
            return LocalDate.parse(rawValue);
        } catch (DateTimeParseException exception) {
            throw new InvalidPayloadException(field, "must be a valid ISO-8601 calendar date.");
        }
    }

    private RatePeriod parseRatePeriod(String rawValue) {
        try {
            return RatePeriod.valueOf(rawValue);
        } catch (IllegalArgumentException exception) {
            throw new InvalidPayloadException("financialScenario.fundingRatePeriod", "must be DAY, MONTH or YEAR.");
        }
    }

    private DayCountBasis parseDayCountBasis(Integer rawValue) {
        return switch (rawValue) {
            case 30 -> DayCountBasis.THIRTY;
            case 360 -> DayCountBasis.THREE_SIXTY;
            case 365 -> DayCountBasis.THREE_SIXTY_FIVE;
            default -> throw new InvalidPayloadException(
                    "financialScenario.dayCountBasis",
                    "must be 30, 360 or 365."
            );
        };
    }

    private String format(BigDecimal value) {
        return value.setScale(OUTPUT_SCALE, RoundingMode.HALF_EVEN).toPlainString();
    }
}
