package me.stinper.jwtauth.controller;

import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import me.stinper.jwtauth.dto.JwtResponse;
import me.stinper.jwtauth.dto.RefreshAccessTokenRequest;
import me.stinper.jwtauth.service.authentication.contract.JwtService;
import me.stinper.jwtauth.testutils.ConstraintViolationMockSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for TokenController class")
@ExtendWith(MockitoExtension.class)
class TokenControllerUnitTest {
    @Mock private JwtService jwtService;
    @Mock private jakarta.validation.Validator validator;

    @InjectMocks
    private TokenController tokenController;


    @Test
    void refreshAccessToken_whenRefreshAccessTokenRequestValidationFails_thenThrowsException() {
        //GIVEN
        final RefreshAccessTokenRequest invalidRefreshAccessTokenRequest = new RefreshAccessTokenRequest(
                "REFRESH_TOKEN"
        );

        final String errorMessage = "Error message";

        ConstraintViolationMockSupport.mockValidatorToReturnSingleConstraintViolation(
                validator, errorMessage, invalidRefreshAccessTokenRequest);

        //WHEN & THEN
        assertThatThrownBy(() -> tokenController.refreshAccessToken(invalidRefreshAccessTokenRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(errorMessage);

        verifyNoInteractions(jwtService);
    }


    @Test
    void refreshAccessToken_whenServiceThrowsException_thenThrowsException() {
        //GIVEN
        final RefreshAccessTokenRequest refreshAccessTokenRequest = new RefreshAccessTokenRequest(
                "REFRESH_TOKEN"
        );

        when(validator.validate(any())).thenReturn(Collections.emptySet());
        doThrow(JwtException.class).when(jwtService).refreshAccessToken(refreshAccessTokenRequest);

        //WHEN & THEN
        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(() -> tokenController.refreshAccessToken(refreshAccessTokenRequest));

        verify(jwtService).refreshAccessToken(refreshAccessTokenRequest);
    }


    @Test
    void refreshAccessToken_whenServiceReturnsValue_thenReturnsSuccessfulResponse() {
        //GIVEN
        final String refreshToken = "REFRESH_TOKEN";

        final RefreshAccessTokenRequest refreshAccessTokenRequest = new RefreshAccessTokenRequest(
                refreshToken
        );

        final JwtResponse jwtResponse = new JwtResponse("ACCESS_TOKEN", refreshToken);

        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(jwtService.refreshAccessToken(refreshAccessTokenRequest)).thenReturn(jwtResponse);

        //WHEN
        ResponseEntity<JwtResponse> response = tokenController.refreshAccessToken(refreshAccessTokenRequest);

        //THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(jwtResponse);

        verify(jwtService).refreshAccessToken(refreshAccessTokenRequest);
    }
}