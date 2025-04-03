package me.stinper.jwtauth.controller;

import me.stinper.jwtauth.dto.JwksDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for JwksController class")
@ExtendWith(MockitoExtension.class)
class JwksControllerUnitTest {
    @Mock private RSAPublicKey publicKey;

    private JwksController jwksController;

    @BeforeEach
    void setUp() {
        jwksController = new JwksController(publicKey);
    }

    @Test
    void getJwks_shouldReturnJwksDtoWithEncodedKeys() {
        // GIVEN
        BigInteger mockModulus = new BigInteger(1, new byte[]{1, 2, 3, 4});
        BigInteger mockExponent = new BigInteger(1, new byte[]{5, 6, 7, 8});

        when(publicKey.getModulus()).thenReturn(mockModulus);
        when(publicKey.getPublicExponent()).thenReturn(mockExponent);

        String expectedModulus = Base64.getUrlEncoder().encodeToString(mockModulus.toByteArray());
        String expectedExponent = Base64.getUrlEncoder().encodeToString(mockExponent.toByteArray());

        // WHEN
        ResponseEntity<JwksDto> response = jwksController.getJwks();

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().keys()).hasSize(1);

        JwksDto.KeyBody key = response.getBody().keys().getFirst();

        assertThat(key.getN()).isEqualTo(expectedModulus);
        assertThat(key.getE()).isEqualTo(expectedExponent);
    }
}