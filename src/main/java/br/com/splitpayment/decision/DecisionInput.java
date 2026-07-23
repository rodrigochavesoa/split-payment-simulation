package br.com.splitpayment.decision;

import java.math.BigDecimal;
import java.util.Objects;

/** Immutable Decision Engine input based on liquidity coverage and projected split cash balance. */
public record DecisionInput(
        BigDecimal liquidityFixedObligationCoverageIndex,
        BigDecimal minimumProjectedCashBalance,
        BigDecimal minimumCashReserve
) {
    public DecisionInput {
        Objects.requireNonNull(liquidityFixedObligationCoverageIndex, "liquidityFixedObligationCoverageIndex is required.");
        Objects.requireNonNull(minimumProjectedCashBalance, "minimumProjectedCashBalance is required.");
        Objects.requireNonNull(minimumCashReserve, "minimumCashReserve is required.");
        if (minimumCashReserve.signum() < 0) {
            throw new IllegalArgumentException("minimumCashReserve cannot be negative.");
        }
    }
}
