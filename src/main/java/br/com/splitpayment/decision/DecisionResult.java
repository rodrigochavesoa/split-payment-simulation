package br.com.splitpayment.decision;

import java.util.Objects;

/** Immutable, explainable outcome produced exclusively from the supplied index. */
public record DecisionResult(
        ReadinessStatus readinessStatus,
        RiskLevel riskLevel,
        String analyticalMessage
) {
    public DecisionResult {
        Objects.requireNonNull(readinessStatus, "readinessStatus is required.");
        Objects.requireNonNull(riskLevel, "riskLevel is required.");
        if (analyticalMessage == null || analyticalMessage.isBlank()) {
            throw new IllegalArgumentException("analyticalMessage is required.");
        }
    }
}
