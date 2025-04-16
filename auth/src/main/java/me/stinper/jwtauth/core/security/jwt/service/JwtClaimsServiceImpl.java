package me.stinper.jwtauth.core.security.jwt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.security.jwt.JwtSignatureKeysProvider;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtClaimsServiceImpl implements JwtClaimsService {
    private final JwtSignatureKeysProvider signatureKeysProvider;

    @Override
    public Claims parseTokenClaims(@NonNull String token) {
        return Jwts.parser()
                .verifyWith(signatureKeysProvider.publicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
