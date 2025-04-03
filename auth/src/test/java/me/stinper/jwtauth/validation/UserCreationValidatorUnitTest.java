package me.stinper.jwtauth.validation;

import me.stinper.jwtauth.core.error.UserErrorCode;
import me.stinper.jwtauth.dto.user.UserCreationRequest;
import me.stinper.jwtauth.exception.ValidatorUnsupportedTypeException;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.SimpleErrors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test for UserCreationValidator class")
class UserCreationValidatorUnitTest {
    @Mock private UserRepository userRepository;
    @Mock private MessageSourceHelper messageSourceHelper;

    @InjectMocks
    private UserCreationValidator userCreationValidator;

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
                .isThrownBy(() -> userCreationValidator.validate(UNSUPPORTED_OBJECT, testData.SIMPLE_ERRORS));

        verifyNoInteractions(userRepository);
    }


    @Test
    void validate_whenEmailIsUnique_thenDoesNotRejectValue() {
        //GIVEN
        when(userRepository.existsByEmailIgnoreCase(testData.USER_CREATION_REQUEST.email())).thenReturn(false);

        //WHEN
        userCreationValidator.validate(testData.USER_CREATION_REQUEST, testData.SIMPLE_ERRORS);

        //THEN
        assertThat(testData.SIMPLE_ERRORS.getAllErrors()).isEmpty();

        verify(userRepository, times(1)).existsByEmailIgnoreCase(testData.USER_CREATION_REQUEST.email());
    }


    @Test
    void validate_whenEmailIsNotUnique_thenRejectValue() {
        //GIVEN
        final String email = testData.USER_CREATION_REQUEST.email(), errorMessage = "ERROR_MESSAGE";

        when(userRepository.existsByEmailIgnoreCase(email)).thenReturn(true); //Email is NOT unique
        when(messageSourceHelper.getLocalizedMessage(any(), any())).thenReturn(errorMessage);

        //WHEN
        userCreationValidator.validate(testData.USER_CREATION_REQUEST, testData.SIMPLE_ERRORS);

        //THEN
        assertThat(testData.SIMPLE_ERRORS.getFieldErrors())
                .hasSize(1)
                .satisfies(fieldErrors -> {
                    FieldError emailFieldError = fieldErrors.getFirst();

                    assertThat(emailFieldError.getField()).isEqualTo("email");
                    assertThat(emailFieldError.getCode()).isEqualTo(UserErrorCode.EMAIL_NOT_UNIQUE.getCode());
                    assertThat(emailFieldError.getDefaultMessage()).isEqualTo(errorMessage);
                });

        verify(userRepository, times(1)).existsByEmailIgnoreCase(email);
    }


    private static class TestData {
        final UserCreationRequest USER_CREATION_REQUEST = new UserCreationRequest(
                "user@gmail.com", "123"
        );

        final Errors SIMPLE_ERRORS = new SimpleErrors(USER_CREATION_REQUEST);
    }

}
