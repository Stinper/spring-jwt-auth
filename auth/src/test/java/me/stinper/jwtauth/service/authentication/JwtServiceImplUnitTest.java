package me.stinper.jwtauth.service.authentication;

import io.jsonwebtoken.JwtException;
import me.stinper.jwtauth.core.security.jwt.service.JwtCreationService;
import me.stinper.jwtauth.core.security.jwt.service.JwtVerificationService;
import me.stinper.jwtauth.dto.JwtResponse;
import me.stinper.jwtauth.dto.RefreshAccessTokenRequest;
import me.stinper.jwtauth.entity.RefreshToken;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test for JwtServiceImpl class")
class JwtServiceImplUnitTest {
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtVerificationService jwtVerificationService;
    @Mock private JwtCreationService jwtCreationService;
    private final Duration refreshTokenExpiration = Duration.ofDays(14);

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        this.jwtService = new JwtServiceImpl(refreshTokenRepository, jwtVerificationService, jwtCreationService);
        jwtService.setRefreshTokenExpiration(refreshTokenExpiration);
    }


    @Test
    @DisplayName("[#refreshAccessToken]: Checks that the method generates a new access token if the request is valid")
    void refreshAccessToken_whenRequestIsValid_thenRefreshesAccessToken() {
        //GIVEN
        RefreshAccessTokenRequest validRefreshAccessTokenRequest = new RefreshAccessTokenRequest(
                "REFRESH_TOKEN"
        );

        final String refreshTokenFromRequest = validRefreshAccessTokenRequest.refreshToken(),
                newAccessToken = "NEW_ACCESS_TOKEN";

        User user = User.builder()
                .uuid(UUID.randomUUID())
                .email("user@gmail.com")
                .password("123")
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token(refreshTokenFromRequest)
                .user(user)
                .expiresAt(Instant.now().plus(this.refreshTokenExpiration))
                .build();

        //No Exception -> Verification Successful
        doNothing()
                .when(jwtVerificationService)
                .verifyTokenSignature(refreshTokenFromRequest);

        when(refreshTokenRepository.findByToken(refreshTokenFromRequest)).thenReturn(Optional.of(refreshToken));
        when(jwtCreationService.createAccessToken(user)).thenReturn(newAccessToken);

        //WHEN
        JwtResponse jwtResponse = jwtService.refreshAccessToken(validRefreshAccessTokenRequest);

        //THEN
        assertThat(jwtResponse.accessToken()).isEqualTo(newAccessToken);
        assertThat(jwtResponse.refreshToken()).isEqualTo(refreshTokenFromRequest);

        verify(jwtVerificationService).verifyTokenSignature(refreshTokenFromRequest);
        verify(refreshTokenRepository).findByToken(refreshTokenFromRequest);
        verify(jwtCreationService).createAccessToken(user);
    }


    @Test
    @DisplayName(
            """
            [#refreshAccessToken]: Checks that the method throws an exception if the refresh token
             passed in the request is not found in the database
            """
    )
    void refreshAccessToken_whenRefreshTokenNotFound_thenThrowsException() {
        //GIVEN
        RefreshAccessTokenRequest invalidRefreshAccessTokenRequest = new RefreshAccessTokenRequest(
                "INVALID_REFRESH_TOKEN"
        );

        //No Exception -> Verification Successful
        doNothing()
                .when(jwtVerificationService)
                .verifyTokenSignature(invalidRefreshAccessTokenRequest.refreshToken());

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        //WHEN & THEN
        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(() -> jwtService.refreshAccessToken(invalidRefreshAccessTokenRequest));

        verifyNoInteractions(jwtCreationService);
    }


    @Test
    @DisplayName(
            """
            [#refreshAccessToken]: Checks that the method throws an exception if the refresh token
             verification fails
            """
    )
    void refreshAccessToken_whenTokenVerificationFails_thenThrowsException() {
        //GIVEN
        RefreshAccessTokenRequest invalidRefreshAccessTokenRequest = new RefreshAccessTokenRequest(
                "EXPIRED_REFRESH_TOKEN"
        );

        //Exception -> Verification Failed
        doThrow(JwtException.class)
                .when(jwtVerificationService)
                .verifyTokenSignature(invalidRefreshAccessTokenRequest.refreshToken());


        //WHEN & THEN
        assertThatThrownBy(() -> jwtService.refreshAccessToken(invalidRefreshAccessTokenRequest))
                .isInstanceOf(JwtException.class);

        verifyNoInteractions(jwtCreationService);
    }


    @Test
    void generateTokensPair_whenMethodCalls_thenReturnsTokensPair() {
        //GIVEN
        final String accessToken = "ACCESS_TOKEN",
                refreshToken = "REFRESH_TOKEN";

        User user = User.builder()
                .uuid(UUID.randomUUID())
                .email("user@gmail.com")
                .password("123")
                .build();

        when(jwtCreationService.createAccessToken(user)).thenReturn(accessToken);
        when(jwtCreationService.createRefreshToken(user)).thenReturn(refreshToken);

        //WHEN
        JwtResponse jwtResponse = jwtService.generateTokensPair(user);

        //THEN
        assertThat(jwtResponse.accessToken()).isEqualTo(accessToken);
        assertThat(jwtResponse.refreshToken()).isEqualTo(refreshToken);

        verify(refreshTokenRepository).save(
                argThat(token -> token.getUser().equals(user) && token.getToken().equals(refreshToken))
        );
        verify(jwtCreationService).createAccessToken(user);
        verify(jwtCreationService).createRefreshToken(user);
    }


    @Test
    void invalidateRefreshTokens_whenMethodCalls_thenInvalidatesAllRefreshTokensForUser() {
        //GIVEN
        User user = User.builder()
                .uuid(UUID.randomUUID())
                .email("user@gmail.com")
                .password("123")
                .build();

        //WHEN
        jwtService.invalidateRefreshTokens(user);

        //THEN
        verify(refreshTokenRepository).deleteByUser_Email(user.getEmail());
    }
}