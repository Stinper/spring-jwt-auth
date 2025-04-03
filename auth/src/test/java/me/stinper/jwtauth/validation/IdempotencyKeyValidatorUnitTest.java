package me.stinper.jwtauth.validation;

import me.stinper.jwtauth.core.error.IdempotencyKeyErrorCode;
import me.stinper.jwtauth.exception.ValidatorUnsupportedTypeException;
import me.stinper.jwtauth.repository.IdempotencyKeyRepository;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.SimpleErrors;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test for IdempotencyKeyValidator class")
class IdempotencyKeyValidatorUnitTest {
    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;
    @Mock
    private MessageSourceHelper messageSourceHelper;

    @InjectMocks
    private IdempotencyKeyValidator idempotencyKeyValidator;

    private TestData testData;

    @BeforeEach
    void setUp() {
        this.testData = new TestData();
    }

    @Test
    void validate_whenTargetObjectIsNotSupported_thenThrowsException() {
        //GIVEN
        final Integer UNSUPPORTED_OBJECT = 1;

        //WHEN & THEN
        assertThatExceptionOfType(ValidatorUnsupportedTypeException.class)
                .isThrownBy(() -> idempotencyKeyValidator.validate(UNSUPPORTED_OBJECT, testData.SIMPLE_ERRORS));

        verifyNoInteractions(idempotencyKeyRepository);
    }


    @Test
    void validate_whenIdempotencyKeyIsUnique_thenDoesNotRejectValue() {
        //GIVEN
        when(idempotencyKeyRepository.existsByKey(testData.IDEMPOTENCY_KEY)).thenReturn(false);

        //WHEN
        idempotencyKeyValidator.validate(testData.IDEMPOTENCY_KEY, testData.SIMPLE_ERRORS);

        //THEN
        assertThat(testData.SIMPLE_ERRORS.getAllErrors()).isEmpty();
        verify(idempotencyKeyRepository, times(1)).existsByKey(testData.IDEMPOTENCY_KEY);
    }


    @Test
    void validate_whenIdempotencyKeyIsNotUnique_thenRejectsValue() {
        //GIVEN
        final String errorMessage = "ERROR_MESSAGE";
        when(idempotencyKeyRepository.existsByKey(testData.IDEMPOTENCY_KEY)).thenReturn(true);
        when(messageSourceHelper.getLocalizedMessage(any(), any())).thenReturn(errorMessage);

        //WHEN
        idempotencyKeyValidator.validate(testData.IDEMPOTENCY_KEY, testData.SIMPLE_ERRORS);

        //THEN
        assertThat(testData.SIMPLE_ERRORS.getAllErrors())
                .hasSize(1)
                .satisfies(errors -> {
                    ObjectError idempotencyKeyError = errors.getFirst();

                    assertThat(idempotencyKeyError.getCode()).isEqualTo(IdempotencyKeyErrorCode.KEY_NOT_UNIQUE.getCode());
                    assertThat(idempotencyKeyError.getDefaultMessage()).isEqualTo(errorMessage);
                });

        verify(idempotencyKeyRepository, times(1)).existsByKey(testData.IDEMPOTENCY_KEY);
    }


    private static class TestData {
        final UUID IDEMPOTENCY_KEY = UUID.fromString("0195853e-396f-7369-bebe-3b2b0ca34c8e");

        final Errors SIMPLE_ERRORS = new SimpleErrors(IDEMPOTENCY_KEY);
    }
}
