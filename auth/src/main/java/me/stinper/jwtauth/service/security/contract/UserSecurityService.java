package me.stinper.jwtauth.service.security.contract;

import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.service.entity.support.UserFilterStrategy;
import org.springframework.lang.NonNull;

import java.util.UUID;

public interface UserSecurityService {
    boolean isAllowedToFindAllUsers(@NonNull JwtAuthUserDetails user);

    boolean isAllowedToFindUserByUUID(@NonNull UUID targetUserUUID, @NonNull JwtAuthUserDetails user);

    boolean isAllowedToDeleteAccount(@NonNull UUID targetAccountUuid, @NonNull JwtAuthUserDetails userDetails);

    UserFilterStrategy chooseUserFilterStrategy(@NonNull JwtAuthUserDetails user);
}
