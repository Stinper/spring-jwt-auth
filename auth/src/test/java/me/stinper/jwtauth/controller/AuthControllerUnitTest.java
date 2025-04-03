package me.stinper.jwtauth.controller;

import jakarta.validation.ConstraintViolationException;
import me.stinper.jwtauth.dto.JwtResponse;
import me.stinper.jwtauth.dto.user.LoginRequest;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.exception.ResourceNotFoundException;
import me.stinper.jwtauth.service.authentication.contract.AuthService;
import me.stinper.jwtauth.service.entity.contract.IdempotencyService;
import me.stinper.jwtauth.testutils.ConstraintViolationMockSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for AuthController class")
@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {
    @Mock private AuthService authService;
    @Mock private IdempotencyService idempotencyService;
    @Mock private jakarta.validation.Validator validator;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_whenLoginRequestValidationFails_thenThrowsException() {
        //GIVEN
        final LoginRequest invalidLoginRequest = new LoginRequest(
                "user@gmail.com", "123"
        );

        final String errorMessage = "Error message";

        ConstraintViolationMockSupport.mockValidatorToReturnSingleConstraintViolation(
                validator, errorMessage, invalidLoginRequest);

        //WHEN & THEN
        assertThatThrownBy(() -> authController.login(UUID.randomUUID(), invalidLoginRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(errorMessage);
    }


    @ParameterizedTest
    @MethodSource("exceptionsSource")
    void login_whenLoginFails_thenThrowsException(Class<? extends Throwable> exceptionClass) {
        //GIVEN
        final LoginRequest loginRequest = new LoginRequest(
                "user@gmail.com", "123"
        );

        doThrow(exceptionClass).when(authService).login(any());
        when(idempotencyService.wrap(any(), any(), any())).thenCallRealMethod();

        //WHEN & THEN
        assertThatThrownBy(() -> authController.login(UUID.randomUUID(), loginRequest))
                .isInstanceOf(exceptionClass);
    }

    private static Stream<Arguments> exceptionsSource() {
        return Stream.of(
                Arguments.of(ResourceNotFoundException.class),
                Arguments.of(BadCredentialsException.class),
                Arguments.of(LockedException.class),
                Arguments.of(DisabledException.class)
        );
    }


    @Test
    void login_whenLoginRequestIsValid_thenReturnsJwtTokensPair() {
        //GIVEN
        final LoginRequest loginRequest = new LoginRequest(
                "user@gmail.com", "123"
        );

        final JwtResponse jwtResponse = new JwtResponse(
                "ACCESS_TOKEN", "REFRESH_TOKEN"
        );

        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(authService.login(loginRequest)).thenReturn(jwtResponse);
        when(idempotencyService.wrap(any(), any(), any())).thenCallRealMethod();

        //WHEN
        ResponseEntity<JwtResponse> response = authController.login(UUID.randomUUID(), loginRequest);

        //THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(jwtResponse);

        verify(idempotencyService).wrap(any(), any(), any());
        verify(authService).login(loginRequest);
    }


    @Test
    void logout_whenCallsAuthService_thenReturnsSuccessfulResponse() {
        //GIVEN
        final User user = User.builder()
                .uuid(UUID.randomUUID())
                .email("user@gmail.com")
                .password("123")
                .build();

        doNothing().when(authService).logout(user);

        //WHEN
        ResponseEntity<?> response = authController.logout(user);

        //THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        verify(authService).logout(user);
    }
}