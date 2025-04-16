package me.stinper.jwtauth.core.security.jwt.service;

import io.jsonwebtoken.Claims;
import org.springframework.lang.NonNull;

public interface JwtClaimsService {
    Claims parseTokenClaims(@NonNull String token);
}
