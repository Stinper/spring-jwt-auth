package me.stinper.jwtauth.core.security.jwt.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.security.jwt.JwtSignatureKeysProvider;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtVerificationServiceImpl implements JwtVerificationService {
    private final JwtSignatureKeysProvider signatureKeysProvider;

    @Override
    public void verifyTokenSignature(@NonNull String token) throws JwtException {
        Jwts.parser()
                .verifyWith(signatureKeysProvider.publicKey())
                .build()
                .parseSignedClaims(token);
    }
}
