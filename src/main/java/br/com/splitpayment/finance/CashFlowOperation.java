package br.com.splitpayment.finance;

import java.math.BigDecimal;
import java.util.Objects;

/** Raw cash variables required to calculate coverage indices for one simulated operation. */
public record CashFlowOperation(
        BigDecimal netRevenues,
        BigDecimal fixedCostCashOutflow,
        BigDecimal variableCostCashOutflow,
        BigDecimal revenueAdjustableAmount
) {
    public CashFlowOperation {
        Objects.requireNonNull(netRevenues, "netRevenues is required.");
        Objects.requireNonNull(fixedCostCashOutflow, "fixedCostCashOutflow is required.");
        Objects.requireNonNull(variableCostCashOutflow, "variableCostCashOutflow is required.");
        Objects.requireNonNull(revenueAdjustableAmount, "revenueAdjustableAmount is required.");
        if (netRevenues.signum() < 0
                || fixedCostCashOutflow.signum() < 0
                || variableCostCashOutflow.signum() < 0
                || revenueAdjustableAmount.signum() < 0) {
            throw new InvalidFinancialScenarioException("Cash-flow values cannot be negative.");
        }
    }
}
