package br.com.splitpayment.api;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = AllowedDayCountBasisValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedDayCountBasis {
    String message() default "must be 30, 360 or 365";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
