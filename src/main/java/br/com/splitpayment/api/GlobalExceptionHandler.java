package br.com.splitpayment.api;

import com.fasterxml.jackson.databind.JsonMappingException;
import br.com.splitpayment.finance.FixedCostCoverageNotApplicableException;
import br.com.splitpayment.finance.AdjustmentNotCalculableException;
import br.com.splitpayment.finance.UnviablePricingModelException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiValidationError> handleValidation(MethodArgumentNotValidException exception) {
        List<FieldErrorDto> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return badRequest("VAL-001", errors);
    }

    @ExceptionHandler(InvalidPayloadException.class)
    public ResponseEntity<ApiValidationError> handleInvalidPayload(InvalidPayloadException exception) {
        return badRequest("VAL-002", exception.field(), exception.getMessage());
    }

    @ExceptionHandler(FixedCostCoverageNotApplicableException.class)
    public ResponseEntity<ApiValidationError> handleFixedCostCoverageNotApplicable(
            FixedCostCoverageNotApplicableException exception
    ) {
        return unprocessableEntity("FIN-001", "operations.fixedCostCashOutflow", exception.getMessage());
    }

    @ExceptionHandler(AdjustmentNotCalculableException.class)
    public ResponseEntity<ApiValidationError> handleAdjustmentNotCalculable(
            AdjustmentNotCalculableException exception
    ) {
        return unprocessableEntity("FIN-002", "operations.revenueAdjustableAmount", exception.getMessage());
    }

    @ExceptionHandler(UnviablePricingModelException.class)
    public ResponseEntity<ApiValidationError> handleUnviablePricingModel(
            UnviablePricingModelException exception
    ) {
        return unprocessableEntity("FIN-003", "financialScenario.incrementalMargins", exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiValidationError> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        return badRequest("VAL-003", extractJsonField(exception), "must contain valid JSON with correctly typed fields.");
    }

    private FieldErrorDto toFieldError(FieldError error) {
        return new FieldErrorDto(error.getField(), error.getDefaultMessage());
    }

    private ResponseEntity<ApiValidationError> badRequest(String errorCode, List<FieldErrorDto> errors) {
        return ResponseEntity.badRequest().body(new ApiValidationError(
                HttpStatus.BAD_REQUEST.value(),
                errorCode,
                errors
        ));
    }

    private ResponseEntity<ApiValidationError> badRequest(String errorCode, String field, String message) {
        return badRequest(errorCode, List.of(new FieldErrorDto(field, message)));
    }

    private ResponseEntity<ApiValidationError> unprocessableEntity(
            String errorCode,
            String field,
            String message
    ) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ApiValidationError(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                errorCode,
                List.of(new FieldErrorDto(field, message))
        ));
    }

    private String extractJsonField(HttpMessageNotReadableException exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof JsonMappingException mappingException
                    && !mappingException.getPath().isEmpty()) {
                return mappingException.getPath().stream()
                        .map(JsonMappingException.Reference::getFieldName)
                        .filter(field -> field != null && !field.isBlank())
                        .reduce((left, right) -> left + "." + right)
                        .orElse("request");
            }
            current = current.getCause();
        }
        return "request";
    }
}
