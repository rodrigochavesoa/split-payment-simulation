package br.com.splitpayment.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SimulationRequest(
        @NotBlank(message = "must be provided")
        String scenarioId,

        @NotBlank(message = "must be provided")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "must use ISO-8601 date format yyyy-MM-dd")
        String referenceDate,

        @NotBlank(message = "must be provided")
        String rulesetVersion,

        @NotEmpty(message = "must contain at least one operation")
        @Valid
        java.util.List<OperationDto> operations,

        @NotNull(message = "must be provided")
        @Valid
        FinancialScenarioDto financialScenario
) {
}
