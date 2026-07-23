package br.com.splitpayment.tax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class TaxEngineTest {
    private final TaxEngine engine = new TaxEngine();

    // Given a taxable base, effective rate and 100% split eligibility,
    // when Tax Engine calculates, then the full tax debit is segregated.
    @Test
    void segregatesFullTaxDebitForFullyEligibleOperation() {
        TaxCalculationResult result = engine.calculate(input("1000.00", "0.28", "1.00"));

        assertDecimal("280.0000", result.grossTaxDebit());
        assertDecimal("280.0000", result.splitEligibleAmount());
        assertDecimal("280.0000", result.simulatedSplitWithheldAmount());
        assertDecimal("280.0000", result.taxEvent().simulatedSplitWithheldAmount());
    }

    // Given a partially eligible operation, when Tax Engine calculates,
    // then only the configured eligible portion is withheld.
    @Test
    void segregatesOnlyTheConfiguredEligiblePercentage() {
        TaxCalculationResult result = engine.calculate(input("1000.00", "0.28", "0.50"));

        assertDecimal("280.0000", result.grossTaxDebit());
        assertDecimal("140.0000", result.simulatedSplitWithheldAmount());
    }

    // Given part of the gross debit was already extinguished, when Tax Engine calculates,
    // then it subtracts that amount before applying split eligibility.
    @Test
    void deductsAlreadyExtinguishedTaxBeforeApplyingEligibilityPercentage() {
        TaxCalculationResult result = engine.calculate(input("1000.00", "0.28", "80.00", "0.50"));

        assertDecimal("280.0000", result.grossTaxDebit());
        assertDecimal("200.0000", result.splitEligibleAmount());
        assertDecimal("100.0000", result.simulatedSplitWithheldAmount());
    }

    // Given already extinguished tax exceeds the gross debit, when Tax Engine calculates,
    // then the eligible and withheld values are zero and never negative.
    @Test
    void clampsSplitEligibleAmountToZeroWhenExtinguishedTaxExceedsGrossDebit() {
        TaxCalculationResult result = engine.calculate(input("1000.00", "0.28", "300.00", "1.00"));

        assertDecimal("280.0000", result.grossTaxDebit());
        assertDecimal("0.0000", result.splitEligibleAmount());
        assertDecimal("0.0000", result.simulatedSplitWithheldAmount());
    }

    // Given a zero effective tax rate, when Tax Engine calculates,
    // then every tax and split amount is zero.
    @Test
    void returnsZeroAmountsForZeroTaxRate() {
        TaxCalculationResult result = engine.calculate(input("1000.00", "0.00", "1.00"));

        assertDecimal("0.0000", result.grossTaxDebit());
        assertDecimal("0.0000", result.simulatedSplitWithheldAmount());
    }

    // Given a zero taxable base, when Tax Engine calculates,
    // then every tax and split amount is zero.
    @Test
    void returnsZeroAmountsForZeroTaxableBase() {
        TaxCalculationResult result = engine.calculate(input("0.00", "0.28", "1.00"));

        assertDecimal("0.0000", result.grossTaxDebit());
        assertDecimal("0.0000", result.simulatedSplitWithheldAmount());
    }

    // Given two operations, when the Tax Engine aggregates them,
    // then every total equals the exact sum of the individual operation results.
    @Test
    void aggregatesTaxDebitsEligibleAmountsAndSplitWithholdingsAcrossOperations() {
        AggregatedTaxResult result = engine.calculateAll(List.of(
                input("1000.00", "0.28", "0.00", "1.00"),
                input("500.00", "0.20", "20.00", "0.50")
        ));

        assertEquals(2, result.operationResults().size());
        assertEquals(2, result.taxEvents().size());
        assertDecimal("380.0000", result.totalGrossTaxDebit());
        assertDecimal("360.0000", result.totalSplitEligibleAmount());
        assertDecimal("320.0000", result.totalSimulatedSplitWithheldAmount());
    }

    private static TaxCalculationInput input(String base, String rate, String eligibility) {
        return input(base, rate, "0.00", eligibility);
    }

    private static TaxCalculationInput input(String base, String rate, String extinguished, String eligibility) {
        return new TaxCalculationInput(
                "operation-1",
                new BigDecimal(base),
                new BigDecimal(rate),
                new BigDecimal(extinguished),
                new BigDecimal(eligibility),
                LocalDate.parse("2026-01-10"),
                LocalDate.parse("2026-02-10")
        );
    }

    private static void assertDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "Expected " + expected + " but was " + actual);
    }
}
