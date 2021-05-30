package io.sylon.wealth.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.beans.BeanUtils.getPropertyDescriptor;

public class UpdateWatchlistValidator implements ConstraintValidator<AnyNotNull, Object> {

  @Override
  public void initialize(AnyNotNull constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
    List<String> fieldNames = Arrays.stream(o.getClass().getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
    List<Object> fieldValues = fieldNames.stream().map(f -> {
      try {
        return Objects.requireNonNull(getPropertyDescriptor(o.getClass(), f)).getReadMethod().invoke(o);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }).collect(Collectors.toList());
    return fieldValues.stream().anyMatch(Objects::nonNull);
  }
}
