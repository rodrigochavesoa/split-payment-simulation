package br.com.splitpayment.tax;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Happy-path input for the parametrized Tax Engine.
 * Sprint 2 accepts a previously extinguished amount supplied by an upstream tax-policy resolver.
 */
public record TaxCalculationInput(
        String operationId,
        BigDecimal taxableBase,
        BigDecimal effectiveTaxRate,
        BigDecimal alreadyExtinguishedTaxAmount,
        BigDecimal splitEligiblePercentage,
        LocalDate settlementDate,
        LocalDate baselineTaxDueDate
) {
    private static final BigDecimal ONE = BigDecimal.ONE;

    public TaxCalculationInput {
        if (operationId == null || operationId.isBlank()) {
            throw new InvalidTaxScenarioException("operationId is required.");
        }
        Objects.requireNonNull(taxableBase, "taxableBase is required.");
        Objects.requireNonNull(effectiveTaxRate, "effectiveTaxRate is required.");
        Objects.requireNonNull(alreadyExtinguishedTaxAmount, "alreadyExtinguishedTaxAmount is required.");
        Objects.requireNonNull(splitEligiblePercentage, "splitEligiblePercentage is required.");
        Objects.requireNonNull(settlementDate, "settlementDate is required.");
        Objects.requireNonNull(baselineTaxDueDate, "baselineTaxDueDate is required.");

        if (taxableBase.signum() < 0) {
            throw new InvalidTaxScenarioException("taxableBase cannot be negative.");
        }
        if (effectiveTaxRate.signum() < 0 || effectiveTaxRate.compareTo(ONE) > 0) {
            throw new InvalidTaxScenarioException("effectiveTaxRate must be between 0 and 1.");
        }
        if (alreadyExtinguishedTaxAmount.signum() < 0) {
            throw new InvalidTaxScenarioException("alreadyExtinguishedTaxAmount cannot be negative.");
        }
        if (splitEligiblePercentage.signum() < 0 || splitEligiblePercentage.compareTo(ONE) > 0) {
            throw new InvalidTaxScenarioException("splitEligiblePercentage must be between 0 and 1.");
        }
    }
}
