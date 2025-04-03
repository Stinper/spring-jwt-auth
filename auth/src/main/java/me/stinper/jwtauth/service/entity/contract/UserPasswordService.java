package me.stinper.jwtauth.service.entity.contract;

import me.stinper.jwtauth.core.security.JwtAuthUserDetails;
import me.stinper.jwtauth.dto.user.PasswordChangeRequest;
import org.springframework.lang.NonNull;

public interface UserPasswordService {

    void changePassword(@NonNull PasswordChangeRequest passwordChangeRequest, @NonNull JwtAuthUserDetails userDetails);

}
