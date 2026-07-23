package br.com.splitpayment.api;

public record TaxResultDto(
        String grossTaxDebit,
        String splitEligibleAmount,
        String simulatedSplitWithheldAmount
) {
}
