package br.com.splitpayment.api;

public record DecisionResultDto(
        String readinessStatus,
        String riskLevel,
        String analyticalMessage
) {
}
