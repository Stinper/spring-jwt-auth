package me.stinper.jwtauth.service.security;

import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.service.security.contract.UserSecurityService;
import me.stinper.jwtauth.utils.SecurityUtils;
import org.springframework.lang.NonNull;

import java.util.UUID;

public class UserSecurityServiceImpl implements UserSecurityService {

    @Override
    public boolean isAllowedToDeleteAccount(@NonNull UUID targetAccountUuid, @NonNull JwtAuthUserDetails userDetails) {
        return !userDetails.getUuid().equals(targetAccountUuid) && //Нельзя удалить свою собственную учетную запись
                        SecurityUtils.hasAuthority(userDetails, "ROLE_ADMIN"); //Удалять учетные записи может только админ
    }

}
