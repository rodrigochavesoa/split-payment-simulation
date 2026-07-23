package br.com.splitpayment.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record FinancialScenarioDto(
        @NotBlank(message = "must be provided")
        @JsonDeserialize(using = StrictDecimalStringDeserializer.class)
        @Pattern(regexp = "^(?:0|0\\.\\d{1,8}|[1-9]\\d*(?:\\.\\d{1,8})?)$", message = "must be a non-negative decimal string using a dot")
        String fundingRate,

        @NotBlank(message = "must be provided")
        @Pattern(regexp = "^(DAY|MONTH|YEAR)$", message = "must be DAY, MONTH or YEAR")
        String fundingRatePeriod,

        @NotNull(message = "must be provided")
        @AllowedDayCountBasis
        Integer dayCountBasis,

        @NotBlank(message = "must be provided")
        @JsonDeserialize(using = StrictDecimalStringDeserializer.class)
        @Pattern(regexp = "^\\d{1,18}(?:\\.\\d{1,8})?$", message = "must be a non-negative decimal string using a dot")
        String initialAvailableCash,

        @NotBlank(message = "must be provided")
        @JsonDeserialize(using = StrictDecimalStringDeserializer.class)
        @Pattern(regexp = "^\\d{1,18}(?:\\.\\d{1,8})?$", message = "must be a non-negative decimal string using a dot")
        String minimumCashReserve,

        @NotBlank(message = "must be provided")
        @JsonDeserialize(using = StrictDecimalStringDeserializer.class)
        @Pattern(regexp = "^(?:0|0\\.\\d{1,8}|1(?:\\.0{1,8})?)$", message = "must be a decimal between 0 and 1 using a dot")
        String incrementalVariableCostPercentage,

        @NotBlank(message = "must be provided")
        @JsonDeserialize(using = StrictDecimalStringDeserializer.class)
        @Pattern(regexp = "^(?:0|0\\.\\d{1,8}|1(?:\\.0{1,8})?)$", message = "must be a decimal between 0 and 1 using a dot")
        String incrementalPaymentFeePercentage,

        @NotBlank(message = "must be provided")
        @JsonDeserialize(using = StrictDecimalStringDeserializer.class)
        @Pattern(regexp = "^(?:0|0\\.\\d{1,8}|1(?:\\.0{1,8})?)$", message = "must be a decimal between 0 and 1 using a dot")
        String incrementalCommissionPercentage
) {
}
