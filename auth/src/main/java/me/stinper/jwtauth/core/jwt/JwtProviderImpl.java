package me.stinper.jwtauth.core.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import me.stinper.jwtauth.core.security.JwtAuthUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtProviderImpl implements JwtProvider {

    @Value("${app.auth.security.jwt.access-token-expiration}")
    private Duration accessTokenExpiration;

    @Value("${app.auth.security.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Value("${app.auth.security.jwt.public-key}")
    private RSAPublicKey publicKey;

    @Value("${app.auth.security.jwt.private-key}")
    private RSAPrivateKey privateKey;

    @Override
    public String generateAccessToken(@NonNull JwtAuthUserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUuid().toString())
                .expiration(this.getTokenExpirationDate(this.accessTokenExpiration))
                .claim("authorities", userDetails.getAuthorities())
                .claim("email", userDetails.getUsername())
                .signWith(this.privateKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(@NonNull JwtAuthUserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUuid().toString())
                .expiration(this.getTokenExpirationDate(this.refreshTokenExpiration))
                .signWith(this.privateKey)
                .compact();
    }

    @Override
    public void verifyAccessToken(@NonNull String token) throws JwtException {
        this.verifyToken(token, this.publicKey);
    }

    @Override
    public void verifyRefreshToken(@NonNull String token) throws JwtException {
        this.verifyToken(token, this.publicKey);
    }

    @Override
    public Claims accessTokenClaims(@NonNull String accessToken) {
        return this.parseTokenClaims(accessToken, this.publicKey);
    }

    @Override
    public Claims refreshTokenClaims(@NonNull String refreshToken) {
        return this.parseTokenClaims(refreshToken, this.publicKey);
    }

    @Override
    public Header accessTokenHeader(@NonNull String accessToken) {
        return this.parseTokenHeader(accessToken, this.publicKey);
    }

    @Override
    public Header refreshTokenHeader(@NonNull String refreshToken) {
        return this.parseTokenHeader(refreshToken, this.publicKey);
    }

    private Date getTokenExpirationDate(Duration tokenExpiration) {
        Instant accessExpirationInstant = LocalDateTime.now()
                .plus(tokenExpiration)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        return Date.from(accessExpirationInstant);
    }

    private void verifyToken(@NonNull String token, @NonNull PublicKey publicKey) throws JwtException {
        Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token);
    }

    private Claims parseTokenClaims(@NonNull String token, @NonNull PublicKey publicKey) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Header parseTokenHeader(@NonNull String token, @NonNull PublicKey publicKey) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getHeader();
    }
}
