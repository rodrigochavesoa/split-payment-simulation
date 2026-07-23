package br.com.splitpayment.finance;

public final class InvalidFinancialScenarioException extends RuntimeException {
    public InvalidFinancialScenarioException(String message) {
        super(message);
    }
}
