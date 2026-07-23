package br.com.splitpayment.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Immutable event produced by the Tax Engine and consumed by the Finance Engine. */
public record TaxEvent(
        String operationId,
        BigDecimal simulatedSplitWithheldAmount,
        LocalDate settlementDate,
        LocalDate baselineTaxDueDate
) {
    public TaxEvent {
        if (operationId == null || operationId.isBlank()) {
            throw new InvalidFinancialScenarioException("operationId is required.");
        }
        Objects.requireNonNull(simulatedSplitWithheldAmount, "simulatedSplitWithheldAmount is required.");
        Objects.requireNonNull(settlementDate, "settlementDate is required.");
        Objects.requireNonNull(baselineTaxDueDate, "baselineTaxDueDate is required.");
        if (simulatedSplitWithheldAmount.signum() < 0) {
            throw new InvalidFinancialScenarioException("simulatedSplitWithheldAmount cannot be negative.");
        }
    }
}
