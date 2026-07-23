package br.com.splitpayment.api;

import java.util.List;

/** Immutable metadata that explains which declared policies governed a simulation. */
public record SimulationAuditDto(
        String rulesetVersion,
        List<OperationAuditDto> operations
) {
}
