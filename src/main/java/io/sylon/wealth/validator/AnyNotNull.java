package io.sylon.wealth.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint (validatedBy = UpdateWatchlistValidator.class)
@Target (ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)
public @interface AnyNotNull {

  String message() default "At least one field required";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
