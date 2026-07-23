package br.com.splitpayment.tax;

import br.com.splitpayment.finance.TaxEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Stateless pure Domain Service for Sprint 1's parametrized tax calculation.
 * It deliberately does not resolve legal rules or exemptions; prior tax extinction is supplied as input.
 */
public final class TaxEngine {
    public TaxCalculationResult calculate(TaxCalculationInput input) {
        Objects.requireNonNull(input, "input is required.");

        BigDecimal grossTaxDebit = input.taxableBase().multiply(input.effectiveTaxRate());
        BigDecimal splitEligibleAmount = grossTaxDebit.subtract(input.alreadyExtinguishedTaxAmount());
        if (splitEligibleAmount.signum() < 0) {
            splitEligibleAmount = BigDecimal.ZERO;
        }
        BigDecimal simulatedSplitWithheldAmount = splitEligibleAmount.multiply(input.splitEligiblePercentage());

        TaxEvent taxEvent = new TaxEvent(
                input.operationId(),
                simulatedSplitWithheldAmount,
                input.settlementDate(),
                input.baselineTaxDueDate()
        );

        return new TaxCalculationResult(
                input.operationId(),
                grossTaxDebit,
                splitEligibleAmount,
                simulatedSplitWithheldAmount,
                taxEvent
        );
    }

    public AggregatedTaxResult calculateAll(List<TaxCalculationInput> inputs) {
        Objects.requireNonNull(inputs, "inputs is required.");
        if (inputs.isEmpty()) {
            throw new InvalidTaxScenarioException("inputs must contain at least one operation.");
        }

        List<TaxCalculationResult> operationResults = inputs.stream()
                .map(this::calculate)
                .toList();
        List<TaxEvent> taxEvents = operationResults.stream()
                .map(TaxCalculationResult::taxEvent)
                .toList();

        BigDecimal totalGrossTaxDebit = operationResults.stream()
                .map(TaxCalculationResult::grossTaxDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSplitEligibleAmount = operationResults.stream()
                .map(TaxCalculationResult::splitEligibleAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSimulatedSplitWithheldAmount = operationResults.stream()
                .map(TaxCalculationResult::simulatedSplitWithheldAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AggregatedTaxResult(
                operationResults,
                taxEvents,
                totalGrossTaxDebit,
                totalSplitEligibleAmount,
                totalSimulatedSplitWithheldAmount
        );
    }
}
