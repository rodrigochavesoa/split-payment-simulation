package br.com.splitpayment.tax;

public final class InvalidTaxScenarioException extends RuntimeException {
    public InvalidTaxScenarioException(String message) {
        super(message);
    }
}
