package me.stinper.jwtauth.controller;

import me.stinper.jwtauth.dto.JwksDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;

@RestController
public class JwksController {
    private final RSAPublicKey publicKey;

    public JwksController(@Value("${app.auth.security.jwt.public-key}") RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<JwksDto> getJwks() {

        String modulus = Base64.getUrlEncoder().encodeToString(publicKey.getModulus().toByteArray());
        String exponent = Base64.getUrlEncoder().encodeToString(publicKey.getPublicExponent().toByteArray());

        return ResponseEntity
                .ok(new JwksDto(
                        List.of(
                                JwksDto.KeyBody.RSA_KEY
                                        .n(modulus)
                                        .e(exponent)
                                        .build()
                        )
                ));
    }

}
