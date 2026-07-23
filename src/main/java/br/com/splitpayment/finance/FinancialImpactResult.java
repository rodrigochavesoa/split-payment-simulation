package br.com.splitpayment.finance;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * netFinancialImpact: positive means cost from earlier segregation; negative means postponement benefit.
 * All amounts are rounded only when this result is created.
 */
public record FinancialImpactResult(
        BigDecimal netFinancialImpact,
        BigDecimal taxFloatLossCost,
        BigDecimal taxPaymentPostponementBenefit,
        BigDecimal operationalFixedObligationCoverageIndex,
        BigDecimal liquidityFixedObligationCoverageIndex
) {
    public FinancialImpactResult {
        Objects.requireNonNull(netFinancialImpact, "netFinancialImpact is required.");
        Objects.requireNonNull(taxFloatLossCost, "taxFloatLossCost is required.");
        Objects.requireNonNull(taxPaymentPostponementBenefit, "taxPaymentPostponementBenefit is required.");
        Objects.requireNonNull(operationalFixedObligationCoverageIndex, "operationalFixedObligationCoverageIndex is required.");
        Objects.requireNonNull(liquidityFixedObligationCoverageIndex, "liquidityFixedObligationCoverageIndex is required.");
        if (taxFloatLossCost.signum() < 0 || taxPaymentPostponementBenefit.signum() < 0) {
            throw new InvalidFinancialScenarioException("Cost and benefit cannot be negative.");
        }
    }
}
