package br.com.splitpayment.finance;

public final class UnviablePricingModelException extends RuntimeException {
    public UnviablePricingModelException() {
        super("Estimated price adjustment is unviable because incremental contribution margin is zero or negative.");
    }
}
