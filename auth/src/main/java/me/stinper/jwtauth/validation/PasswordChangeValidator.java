package me.stinper.jwtauth.validation;

import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.error.UserErrorCode;
import me.stinper.jwtauth.dto.user.PasswordChangeRequest;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.exception.ValidatorUnsupportedTypeException;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import me.stinper.jwtauth.utils.SecurityUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class PasswordChangeValidator implements Validator {
    private final PasswordEncoder passwordEncoder;
    private final MessageSourceHelper messageSourceHelper;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return PasswordChangeRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        if (!this.supports(target.getClass()))
            throw new ValidatorUnsupportedTypeException();

        PasswordChangeRequest passwordChangeRequest = (PasswordChangeRequest) target;
        String oldPassword = passwordChangeRequest.oldPassword(),
                newPassword = passwordChangeRequest.newPassword(),
                repeatNewPassword = passwordChangeRequest.repeatNewPassword();

        User currentUser = SecurityUtils.getCurrentUser();

        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            errors.rejectValue(
                    "oldPassword",
                    UserErrorCode.WRONG_PASSWORD.getCode(),
                    messageSourceHelper.getLocalizedMessage("messages.user.password-change.wrong-old-password")
            );
        }

        if (oldPassword.equals(newPassword)) {
            errors.rejectValue(
                    "newPassword",
                    UserErrorCode.OLD_AND_NEW_PASSWORDS_MATCHES.getCode(),
                    messageSourceHelper.getLocalizedMessage("messages.user.password-change.identical-old-and-new-passwords")
            );

        }

        if (!repeatNewPassword.equals(newPassword)) {
            errors.rejectValue(
                    "repeatNewPassword",
                    UserErrorCode.PASSWORDS_DO_NOT_MATCH.getCode(),
                    messageSourceHelper.getLocalizedMessage("messages.user.password-change.old-and-new-passwords-do-not-match")
            );
        }
    }
}
