package me.stinper.jwtauth.service.security;

import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.security.AuthorityChecker;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.service.entity.support.ActiveUserFilterStrategy;
import me.stinper.jwtauth.service.entity.support.AllUserFilterStrategy;
import me.stinper.jwtauth.service.entity.support.UserFilterStrategy;
import me.stinper.jwtauth.service.security.contract.UserSecurityService;
import org.springframework.lang.NonNull;

import java.util.UUID;

@RequiredArgsConstructor
public class UserSecurityServiceImpl implements UserSecurityService {
    private final AuthorityChecker authorityChecker;
    private final UserRepository userRepository;

    @Override
    public boolean isAllowedToFindAllUsers(@NonNull JwtAuthUserDetails user) {
        return authorityChecker.isAdminOrHasPermission(user, "user.read.find-all-users");
    }

    @Override
    public boolean isAllowedToFindUserByUUID(@NonNull UUID targetUserUUID, @NonNull JwtAuthUserDetails user) {
        if (authorityChecker.isAdmin(user)) {
            return true;
        }

        User targetUser = userRepository.findById(targetUserUUID).orElse(null);

        if (targetUser == null)
            return true; //Сервис обработает ситуацию правильно

        if (targetUser.getDeactivatedAt() == null)
            return authorityChecker.hasAuthority(user, "user.read.find-by-uuid");

        return authorityChecker.hasAllAuthorities(user, "user.read.find-by-uuid", "user.read.read-deactivated-users");
    }

    @Override
    public boolean isAllowedToDeleteAccount(@NonNull UUID targetAccountUuid, @NonNull JwtAuthUserDetails userDetails) {
        if (userDetails.getUuid().equals(targetAccountUuid)) {
            return false;
        }

        return authorityChecker.isAdminOrHasPermission(userDetails, "user.delete.deactivate-by-uuid");
    }

    @Override
    public UserFilterStrategy chooseUserFilterStrategy(@NonNull JwtAuthUserDetails user) {
        if (authorityChecker.isAdminOrHasPermission(user, "user.read.read-deactivated-users")) {
            return new AllUserFilterStrategy();
        }

        return new ActiveUserFilterStrategy();
    }
}
