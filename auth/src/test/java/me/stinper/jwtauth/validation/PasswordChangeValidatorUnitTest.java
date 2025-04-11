package me.stinper.jwtauth.validation;

import me.stinper.jwtauth.core.error.UserErrorCode;
import me.stinper.jwtauth.dto.user.PasswordChangeRequest;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.exception.ValidatorUnsupportedTypeException;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import me.stinper.jwtauth.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.SimpleErrors;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test for PasswordChangeValidator test")
public class PasswordChangeValidatorUnitTest {
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MessageSourceHelper messageSourceHelper;

    @InjectMocks
    private PasswordChangeValidator passwordChangeValidator;

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
                .isThrownBy(() -> passwordChangeValidator.validate(UNSUPPORTED_OBJECT, testData.SIMPLE_ERRORS));

        verifyNoInteractions(passwordEncoder, messageSourceHelper);
    }


    @Test
    @DisplayName("[#validate]: Checks that the method does not reject any values if the request is fully valid")
    void validate_whenRequestIsValid_thenDoesNotRejectAnyFields() {
        withMockedCurrentUser(() -> {
            //GIVEN
            final String oldPassword = testData.PASSWORD_CHANGE_REQUEST.oldPassword();
            final User currentUser = testData.CURRENT_USER;

            when(passwordEncoder.matches(oldPassword, currentUser.getPassword())).thenReturn(true);

            //WHEN
            passwordChangeValidator.validate(testData.PASSWORD_CHANGE_REQUEST, testData.SIMPLE_ERRORS);

            //THEN
            assertThat(testData.SIMPLE_ERRORS.getAllErrors()).isEmpty();
            verify(passwordEncoder).matches(oldPassword, currentUser.getPassword());

            verifyNoInteractions(messageSourceHelper);
        });
    }


    @Test
    @DisplayName("[#validate]: Checks that the method rejects the value of the old password if it is incorrect")
    void validate_whenWrongOldPassword_thenRejectValue() {
        withMockedCurrentUser(() -> {
            //GIVEN
            final String errorMessage = "ERROR_MESSAGE";

            when(passwordEncoder.matches(eq(testData.PASSWORD_CHANGE_REQUEST.oldPassword()), any())).thenReturn(false);
            when(messageSourceHelper.getLocalizedMessage("messages.user.password-change.wrong-old-password")).thenReturn(errorMessage);

            //WHEN
            passwordChangeValidator.validate(testData.PASSWORD_CHANGE_REQUEST, testData.SIMPLE_ERRORS);

            //THEN
            assertThat(testData.SIMPLE_ERRORS.getFieldErrors())
                    .hasSize(1)
                    .satisfies(fieldErrors -> {
                        FieldError oldPasswordFieldError = fieldErrors.getFirst();

                        assertThat(oldPasswordFieldError.getField()).isEqualTo("oldPassword");
                        assertThat(oldPasswordFieldError.getCode()).isEqualTo(UserErrorCode.WRONG_PASSWORD.getCode());
                        assertThat(oldPasswordFieldError.getDefaultMessage()).isEqualTo(errorMessage);
                    });


            verify(passwordEncoder).matches(eq(testData.PASSWORD_CHANGE_REQUEST.oldPassword()), any());
            verify(messageSourceHelper).getLocalizedMessage("messages.user.password-change.wrong-old-password");
        });
    }


    @Test
    @DisplayName("[#validate]: Checks that the method rejects the value of the new password if it matches the old password")
    void validate_whenOldAndNewPasswordsMatches_thenRejectValue() {
        withMockedCurrentUser(() -> {
            //GIVEN
            PasswordChangeRequest invalidPasswordChangeRequest = new PasswordChangeRequest(
                    "OLD_PASSWORD", "OLD_PASSWORD", "OLD_PASSWORD"
            );

            Errors errors = new SimpleErrors(invalidPasswordChangeRequest);

            final String errorMessage = "ERROR_MESSAGE";

            when(passwordEncoder.matches(any(), any())).thenReturn(true);
            when(messageSourceHelper.getLocalizedMessage("messages.user.password-change.identical-old-and-new-passwords"))
                    .thenReturn(errorMessage);

            //WHEN
            passwordChangeValidator.validate(invalidPasswordChangeRequest, errors);

            //THEN
            assertThat(errors.getFieldErrors())
                    .hasSize(1)
                    .satisfies(fieldErrors -> {
                        FieldError oldPasswordFieldError = fieldErrors.getFirst();

                        assertThat(oldPasswordFieldError.getField()).isEqualTo("newPassword");
                        assertThat(oldPasswordFieldError.getCode()).isEqualTo(UserErrorCode.OLD_AND_NEW_PASSWORDS_MATCHES.getCode());
                        assertThat(oldPasswordFieldError.getDefaultMessage()).isEqualTo(errorMessage);
                    });


            verify(passwordEncoder).matches(any(), any());
            verify(messageSourceHelper).getLocalizedMessage("messages.user.password-change.identical-old-and-new-passwords");
        });
    }


    @Test
    @DisplayName("[#validate]: Checks that the method rejects the values of repeating the new password if it does not match the new password")
    void validate_whenPasswordsDoNotMatch_thenRejectsValue() {
        withMockedCurrentUser(() -> {
            //GIVEN
            PasswordChangeRequest invalidPasswordChangeRequest = new PasswordChangeRequest(
                    "OLD_PASSWORD", "NEW_PASSWORD", "NEW_PASSWORD_123"
            );

            Errors errors = new SimpleErrors(invalidPasswordChangeRequest);

            final String errorMessage = "ERROR_MESSAGE";

            when(passwordEncoder.matches(any(), any())).thenReturn(true);
            when(messageSourceHelper.getLocalizedMessage("messages.user.password-change.old-and-new-passwords-do-not-match"))
                    .thenReturn(errorMessage);

            //WHEN
            passwordChangeValidator.validate(invalidPasswordChangeRequest, errors);

            //THEN
            assertThat(errors.getFieldErrors())
                    .hasSize(1)
                    .satisfies(fieldErrors -> {
                        FieldError oldPasswordFieldError = fieldErrors.getFirst();

                        assertThat(oldPasswordFieldError.getField()).isEqualTo("repeatNewPassword");
                        assertThat(oldPasswordFieldError.getCode()).isEqualTo(UserErrorCode.PASSWORDS_DO_NOT_MATCH.getCode());
                        assertThat(oldPasswordFieldError.getDefaultMessage()).isEqualTo(errorMessage);
                    });


            verify(passwordEncoder).matches(any(), any());
            verify(messageSourceHelper).getLocalizedMessage("messages.user.password-change.old-and-new-passwords-do-not-match");
        });
    }


    private void withMockedCurrentUser(Runnable runnable) {
        try(MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUser).thenReturn(testData.CURRENT_USER);

            runnable.run();
        }
    }

    private static class TestData {
        final User CURRENT_USER = User.builder()
                .uuid(UUID.fromString("01958513-b805-715e-9c24-dcd36df10fa7"))
                .email("user@gmail.com")
                .password("123")
                .build();

        final PasswordChangeRequest PASSWORD_CHANGE_REQUEST = new PasswordChangeRequest(
                CURRENT_USER.getPassword(), "1234", "1234"
        );

        final Errors SIMPLE_ERRORS = new SimpleErrors(PASSWORD_CHANGE_REQUEST);

    }
}
