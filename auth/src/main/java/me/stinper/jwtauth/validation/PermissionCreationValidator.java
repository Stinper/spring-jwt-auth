package me.stinper.jwtauth.validation;

import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.error.PermissionErrorCode;
import me.stinper.jwtauth.dto.permission.PermissionCreationRequest;
import me.stinper.jwtauth.exception.ValidatorUnsupportedTypeException;
import me.stinper.jwtauth.repository.PermissionRepository;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class PermissionCreationValidator implements Validator {
    private final PermissionRepository permissionRepository;
    private final MessageSourceHelper messageSourceHelper;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return PermissionCreationRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        if (!this.supports(target.getClass()))
            throw new ValidatorUnsupportedTypeException();

        PermissionCreationRequest request = (PermissionCreationRequest) target;

        if (permissionRepository.existsByPermissionIgnoreCase(request.permission())) {
            errors.rejectValue(
                    "permission",
                    PermissionErrorCode.PERMISSION_NOT_UNIQUE.getCode(),
                    messageSourceHelper.getLocalizedMessage(
                            "messages.permission.validation.fields.permission.not-unique",
                            request.permission()
                    )
            );
        }
    }
}
