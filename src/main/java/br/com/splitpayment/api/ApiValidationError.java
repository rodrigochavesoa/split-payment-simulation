package br.com.splitpayment.api;

import java.util.List;

public record ApiValidationError(int status, String errorCode, List<FieldErrorDto> errors) {
}
