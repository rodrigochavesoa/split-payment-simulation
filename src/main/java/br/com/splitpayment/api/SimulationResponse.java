package br.com.splitpayment.api;

public record SimulationResponse(
        String scenarioId,
        String referenceDate,
        SimulationAuditDto auditTrail,
        TaxResultDto taxResult,
        FinancialImpactDto financialImpact,
        DecisionResultDto decisionResult
) {
}
