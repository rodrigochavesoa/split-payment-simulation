package br.com.splitpayment.tax;

import br.com.splitpayment.finance.TaxEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/** Immutable aggregate and Finance Engine handoff for a simulated operation batch. */
public record AggregatedTaxResult(
        List<TaxCalculationResult> operationResults,
        List<TaxEvent> taxEvents,
        BigDecimal totalGrossTaxDebit,
        BigDecimal totalSplitEligibleAmount,
        BigDecimal totalSimulatedSplitWithheldAmount
) {
    public AggregatedTaxResult {
        Objects.requireNonNull(operationResults, "operationResults is required.");
        Objects.requireNonNull(taxEvents, "taxEvents is required.");
        Objects.requireNonNull(totalGrossTaxDebit, "totalGrossTaxDebit is required.");
        Objects.requireNonNull(totalSplitEligibleAmount, "totalSplitEligibleAmount is required.");
        Objects.requireNonNull(totalSimulatedSplitWithheldAmount, "totalSimulatedSplitWithheldAmount is required.");
        operationResults = List.copyOf(operationResults);
        taxEvents = List.copyOf(taxEvents);
    }
}
