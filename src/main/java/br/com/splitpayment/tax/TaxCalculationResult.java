package br.com.splitpayment.tax;

import br.com.splitpayment.finance.TaxEvent;
import java.math.BigDecimal;
import java.util.Objects;

/** Immutable Tax Engine output and the explicit handoff contract to the Finance Engine. */
public record TaxCalculationResult(
        String operationId,
        BigDecimal grossTaxDebit,
        BigDecimal splitEligibleAmount,
        BigDecimal simulatedSplitWithheldAmount,
        TaxEvent taxEvent
) {
    public TaxCalculationResult {
        Objects.requireNonNull(operationId, "operationId is required.");
        Objects.requireNonNull(grossTaxDebit, "grossTaxDebit is required.");
        Objects.requireNonNull(splitEligibleAmount, "splitEligibleAmount is required.");
        Objects.requireNonNull(simulatedSplitWithheldAmount, "simulatedSplitWithheldAmount is required.");
        Objects.requireNonNull(taxEvent, "taxEvent is required.");
    }
}
