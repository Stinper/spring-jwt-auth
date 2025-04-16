package me.stinper.jwtauth.core.security.jwt.service;

import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import org.springframework.lang.NonNull;

public interface JwtCreationService {
    String createAccessToken(@NonNull JwtAuthUserDetails userDetails);

    String createRefreshToken(@NonNull JwtAuthUserDetails userDetails);
}
