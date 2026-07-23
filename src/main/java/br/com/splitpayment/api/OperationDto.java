package br.com.splitpayment.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OperationDto(
        @NotBlank(message = "must be provided")
        String operationId,

        @NotBlank(message = "must be provided")
        String taxPolicyReference,

        @NotBlank(message = "must be provided")
        @JsonDeserialize(using = StrictDecimalStringDeserializer.class)
        @Pattern(regexp = "^\\d{1,18}(?:\\.\\d{1,8})?$", message = "must be a positive decimal string using a dot")
        String taxableBase,

        @NotBlank(message = "must be provided")
        @JsonDeserialize(using = StrictDecimalStringDeserializer.class)
        @Pattern(regexp = "^(?:0|0\\.\\d{1,8}|1(?:\\.0{1,8})?)$", message = "must be a decimal between 0 and 1 using a dot")
        String effectiveTaxRate,

        @NotBlank(message = "must be provided")
        @JsonDeserialize(using = StrictDecimalStringDeserializer.class)
        @Pattern(regexp = "^\\d{1,18}(?:\\.\\d{1,8})?$", message = "must be a non-negative decimal string using a dot")
        String alreadyExtinguishedTaxAmount,

        @NotBlank(message = "must be provided")
        @JsonDeserialize(using = StrictDecimalStringDeserializer.class)
        @Pattern(regexp = "^(?:0|0\\.\\d{1,8}|1(?:\\.0{1,8})?)$", message = "must be a decimal between 0 and 1 using a dot")
        String splitEligiblePercentage,

        @NotBlank(message = "must be provided")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "must use ISO-8601 date format yyyy-MM-dd")
        String settlementDate,

        @NotBlank(message = "must be provided")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "must use ISO-8601 date format yyyy-MM-dd")
        String baselineTaxDueDate,

        @NotBlank(message = "must be provided")
        @JsonDeserialize(using = StrictDecimalStringDeserializer.class)
        @Pattern(regexp = "^\\d{1,18}(?:\\.\\d{1,8})?$", message = "must be a non-negative decimal string using a dot")
        String fixedCostCashOutflow,

        @NotBlank(message = "must be provided")
        @JsonDeserialize(using = StrictDecimalStringDeserializer.class)
        @Pattern(regexp = "^\\d{1,18}(?:\\.\\d{1,8})?$", message = "must be a non-negative decimal string using a dot")
        String variableCostCashOutflow,

        @NotBlank(message = "must be provided")
        @JsonDeserialize(using = StrictDecimalStringDeserializer.class)
        @Pattern(regexp = "^\\d{1,18}(?:\\.\\d{1,8})?$", message = "must be a non-negative decimal string using a dot")
        String netRevenues,

        @NotBlank(message = "must be provided")
        @JsonDeserialize(using = StrictDecimalStringDeserializer.class)
        @Pattern(regexp = "^\\d{1,18}(?:\\.\\d{1,8})?$", message = "must be a non-negative decimal string using a dot")
        String revenueAdjustableAmount
) {
}
