package me.stinper.jwtauth.service.security.contract;

import me.stinper.jwtauth.core.security.JwtAuthUserDetails;
import org.springframework.lang.NonNull;

import java.util.UUID;

public interface UserSecurityService {

    boolean isAllowedToDeleteAccount(@NonNull UUID targetAccountUuid, @NonNull JwtAuthUserDetails userDetails);

}
