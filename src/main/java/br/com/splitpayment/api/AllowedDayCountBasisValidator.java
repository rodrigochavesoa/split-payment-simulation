package br.com.splitpayment.api;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public final class AllowedDayCountBasisValidator implements ConstraintValidator<AllowedDayCountBasis, Integer> {
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value == null || value == 30 || value == 360 || value == 365;
    }
}
