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

    @Test
    void validate_whenTargetObjectIsNotSupported_thenThrowsException() {
        //GIVEN
        final Integer UNSUPPORTED_OBJECT = 1;

        //WHEN & THEN
        assertThatExceptionOfType(ValidatorUnsupportedTypeException.class)
                .isThrownBy(() -> userCreationValidator.validate(UNSUPPORTED_OBJECT, mock(Errors.class)));

        verifyNoInteractions(userRepository, messageSourceHelper);
    }


    @Test
    void validate_whenEmailIsUnique_thenDoesNotRejectValue() {
        //GIVEN
        final UserCreationRequest userCreationRequest = new UserCreationRequest(
                "user@gmail.com", "123"
        );

        final Errors userCreationErrors = new SimpleErrors(userCreationRequest);

        when(userRepository.existsByEmailIgnoreCase(userCreationRequest.email())).thenReturn(false);

        //WHEN
        userCreationValidator.validate(userCreationRequest, userCreationErrors);

        //THEN
        assertThat(userCreationErrors.getAllErrors()).isEmpty();

        verify(userRepository).existsByEmailIgnoreCase(userCreationRequest.email());
        verifyNoInteractions(messageSourceHelper);
    }


    @Test
    void validate_whenEmailIsNotUnique_thenRejectValue() {
        //GIVEN
        final UserCreationRequest userCreationRequest = new UserCreationRequest(
                "user@gmail.com", "123"
        );

        final Errors userCreationErrors = new SimpleErrors(userCreationRequest);

        final String email = userCreationRequest.email(), errorMessage = "ERROR_MESSAGE";

        when(userRepository.existsByEmailIgnoreCase(email)).thenReturn(true); //Email is NOT unique
        when(messageSourceHelper.getLocalizedMessage(
                "messages.user.validation.fields.email.not-unique",
                email)
        ).thenReturn(errorMessage);

        //WHEN
        userCreationValidator.validate(userCreationRequest, userCreationErrors);

        //THEN
        assertThat(userCreationErrors.getFieldErrors())
                .hasSize(1)
                .satisfies(fieldErrors -> {
                    FieldError emailFieldError = fieldErrors.getFirst();

                    assertThat(emailFieldError.getField()).isEqualTo("email");
                    assertThat(emailFieldError.getCode()).isEqualTo(UserErrorCode.EMAIL_NOT_UNIQUE.getCode());
                    assertThat(emailFieldError.getDefaultMessage()).isEqualTo(errorMessage);
                });

        verify(userRepository).existsByEmailIgnoreCase(email);
        verify(messageSourceHelper).getLocalizedMessage("messages.user.validation.fields.email.not-unique", email);
    }
}
