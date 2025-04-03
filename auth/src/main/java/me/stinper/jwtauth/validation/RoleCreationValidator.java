package me.stinper.jwtauth.validation;

import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.error.RoleErrorCode;
import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.exception.ValidatorUnsupportedTypeException;
import me.stinper.jwtauth.repository.RoleRepository;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class RoleCreationValidator implements Validator {
    private final RoleRepository roleRepository;
    private final MessageSourceHelper messageSourceHelper;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return RoleCreationRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        if(!this.supports(target.getClass()))
            throw new ValidatorUnsupportedTypeException();

        RoleCreationRequest roleCreationRequest = (RoleCreationRequest) target;

        if (roleRepository.existsByRoleNameIgnoreCase(roleCreationRequest.roleName())) {
            errors.rejectValue(
                    "roleName",
                    RoleErrorCode.ROLE_NAME_NOT_UNIQUE.getCode(),
                    messageSourceHelper.getLocalizedMessage(
                            "messages.role.validation.fields.role-name.not-unique",
                            roleCreationRequest.roleName()
                    )
            );
        }

    }
}
