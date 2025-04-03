package me.stinper.jwtauth.service.entity;

import me.stinper.jwtauth.core.security.JwtAuthUserDetails;
import me.stinper.jwtauth.dto.user.PasswordChangeRequest;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.exception.EntityValidationException;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.service.authentication.contract.JwtService;
import me.stinper.jwtauth.service.entity.UserPasswordServiceImpl;
import me.stinper.jwtauth.validation.PasswordChangeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for UserPasswordServiceImpl class")
class UserPasswordServiceImplUnitTest {
    @Mock private PasswordChangeValidator passwordChangeValidator;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;

    @InjectMocks
    private UserPasswordServiceImpl userPasswordService;

    private TestData testData;

    @BeforeEach
    void setUp() {
        this.testData = new TestData();
    }

    @Test
    @DisplayName("Checks that the method changes the user's password when the request is valid")
    void changePassword_whenPasswordChangeRequestIsValid_thenChangesPassword() {
        //GIVEN
        final String oldPassword = "123", newPassword = "1234";

        PasswordChangeRequest validPasswordChangeRequest = new PasswordChangeRequest(
                oldPassword, newPassword, newPassword
        );

        //Empty Errors object (request is valid)
        when(passwordChangeValidator.validateObject(validPasswordChangeRequest)).thenReturn(new SimpleErrors(validPasswordChangeRequest));
        when(passwordEncoder.encode(newPassword)).thenReturn("$2y$10$yK6JXIp0H/rGL4tsXRP0GuxNh603JWnF5ybuPhnHRAt1y7xCA07ri");

        //WHEN
        userPasswordService.changePassword(validPasswordChangeRequest, testData.TARGET_USER);

        //THEN
        assertThat(testData.TARGET_USER.getPassword()).isEqualTo("$2y$10$yK6JXIp0H/rGL4tsXRP0GuxNh603JWnF5ybuPhnHRAt1y7xCA07ri");

        verify(userRepository, times(1)).save(testData.TARGET_USER);
        verify(jwtService, times(1)).invalidateRefreshTokens(testData.TARGET_USER);
    }


    @Test
    @DisplayName("Checks that the method throws an exception when the request is NOT valid")
    void changePassword_whenPasswordChangeRequestIsInvalid_thenThrowsException() {
        //GIVEN
        PasswordChangeRequest invalidPasswordChangeRequest = new PasswordChangeRequest(
            null, null, null
        );

        Errors passwordChangeErrors = new SimpleErrors(invalidPasswordChangeRequest);
        passwordChangeErrors.rejectValue("oldPassword", "ERROR_CODE");

        when(passwordChangeValidator.validateObject(invalidPasswordChangeRequest)).thenReturn(passwordChangeErrors);

        //WHEN & THEN
        assertThatExceptionOfType(EntityValidationException.class)
                .isThrownBy(() -> userPasswordService.changePassword(invalidPasswordChangeRequest, testData.TARGET_USER));

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).invalidateRefreshTokens(any());
    }


    private static class TestData {
        final User TARGET_USER = User.builder()
                .uuid(UUID.randomUUID())
                .email("user@gmail.com")
                .password("123")
                .build();
    }
}
