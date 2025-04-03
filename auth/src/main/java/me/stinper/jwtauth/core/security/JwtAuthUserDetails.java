package me.stinper.jwtauth.core.security;

import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.UUID;

public interface JwtAuthUserDetails extends UserDetails {

    UUID getUuid();

    Instant getRegisteredAt();

    Boolean getIsEmailVerified();

    @Nullable
    Instant getDeactivatedAt();
}
