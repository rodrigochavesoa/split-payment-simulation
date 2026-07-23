package br.com.splitpayment.finance;

public final class AdjustmentNotCalculableException extends RuntimeException {
    public AdjustmentNotCalculableException() {
        super("Estimated price adjustment cannot be calculated when adjustable revenue is zero.");
    }
}
