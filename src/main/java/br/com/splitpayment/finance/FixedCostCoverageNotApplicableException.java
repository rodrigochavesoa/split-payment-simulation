package br.com.splitpayment.finance;

/** Controlled domain exception: coverage is undefined when fixed cash obligations equal zero. */
public final class FixedCostCoverageNotApplicableException extends RuntimeException {
    public FixedCostCoverageNotApplicableException() {
        super("Fixed obligation coverage is not applicable when fixedCostCashOutflow is zero.");
    }
}
