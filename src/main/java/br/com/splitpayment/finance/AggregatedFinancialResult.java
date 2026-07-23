package br.com.splitpayment.finance;

import java.math.BigDecimal;
import java.util.Objects;

/** Immutable financial outcome for the complete simulation batch. */
public record AggregatedFinancialResult(
        BigDecimal totalNetRevenues,
        BigDecimal totalFixedCostCashOutflow,
        BigDecimal totalVariableCostCashOutflow,
        BigDecimal netFinancialImpact,
        BigDecimal taxFloatLossCost,
        BigDecimal taxPaymentPostponementBenefit,
        BigDecimal operationalFixedObligationCoverageIndex,
        BigDecimal liquidityFixedObligationCoverageIndex,
        BigDecimal minimumBaselineProjectedCashBalance,
        BigDecimal minimumSplitProjectedCashBalance,
        BigDecimal cashGap,
        BigDecimal estimatedPriceAdjustmentPercentage
) {
    public AggregatedFinancialResult {
        Objects.requireNonNull(totalNetRevenues, "totalNetRevenues is required.");
        Objects.requireNonNull(totalFixedCostCashOutflow, "totalFixedCostCashOutflow is required.");
        Objects.requireNonNull(totalVariableCostCashOutflow, "totalVariableCostCashOutflow is required.");
        Objects.requireNonNull(netFinancialImpact, "netFinancialImpact is required.");
        Objects.requireNonNull(taxFloatLossCost, "taxFloatLossCost is required.");
        Objects.requireNonNull(taxPaymentPostponementBenefit, "taxPaymentPostponementBenefit is required.");
        Objects.requireNonNull(operationalFixedObligationCoverageIndex, "operationalFixedObligationCoverageIndex is required.");
        Objects.requireNonNull(liquidityFixedObligationCoverageIndex, "liquidityFixedObligationCoverageIndex is required.");
        Objects.requireNonNull(minimumBaselineProjectedCashBalance, "minimumBaselineProjectedCashBalance is required.");
        Objects.requireNonNull(minimumSplitProjectedCashBalance, "minimumSplitProjectedCashBalance is required.");
        Objects.requireNonNull(cashGap, "cashGap is required.");
        Objects.requireNonNull(estimatedPriceAdjustmentPercentage, "estimatedPriceAdjustmentPercentage is required.");
    }
}
