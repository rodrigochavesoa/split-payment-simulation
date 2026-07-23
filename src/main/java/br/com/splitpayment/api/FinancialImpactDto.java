package br.com.splitpayment.api;

public record FinancialImpactDto(
        String totalNetRevenues,
        String totalFixedCostCashOutflow,
        String totalVariableCostCashOutflow,
        String netFinancialImpact,
        String taxFloatLossCost,
        String taxPaymentPostponementBenefit,
        String operationalFixedObligationCoverageIndex,
        String liquidityFixedObligationCoverageIndex,
        String minimumBaselineProjectedCashBalance,
        String minimumSplitProjectedCashBalance,
        String cashGap,
        String estimatedPriceAdjustmentPercentage
) {
}
