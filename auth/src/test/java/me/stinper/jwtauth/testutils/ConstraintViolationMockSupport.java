package me.stinper.jwtauth.testutils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ConstraintViolationMockSupport {
    private ConstraintViolationMockSupport() {}

    /**
     * Метод, который заставляет Mock-объект {@link Validator} возвращать Set из одного объекта
     * типа {@link ConstraintViolation}, имитируя поведение, когда хотя бы одно поле класса не проходит валидацию
     * @param validator Mock-объект валидатора
     * @param errorMessage сообщение об ошибке, которое будет помещено в единственный объект типа {@link ConstraintViolation}
     * @param targetObject объект класса, поля которого не проходят валидацию
     */
    public static <T> void mockValidatorToReturnSingleConstraintViolation(Validator validator, String errorMessage, T targetObject) {
        @SuppressWarnings("unchecked")
        ConstraintViolation<T> violation = mock(ConstraintViolation.class);

        when(violation.getMessage()).thenReturn(errorMessage);
        when(validator.validate(targetObject)).thenReturn(Set.of(violation));
    }
}
