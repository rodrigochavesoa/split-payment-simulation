package br.com.splitpayment.api;

/** Client-declared tax policy reference preserved for auditability. */
public record OperationAuditDto(
        String operationId,
        String taxPolicyReference
) {
}
