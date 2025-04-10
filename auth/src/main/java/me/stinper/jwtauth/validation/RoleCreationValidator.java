package me.stinper.jwtauth.validation;

import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.error.RoleErrorCode;
import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.entity.Permission;
import me.stinper.jwtauth.exception.ValidatorUnsupportedTypeException;
import me.stinper.jwtauth.repository.PermissionRepository;
import me.stinper.jwtauth.repository.RoleRepository;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleCreationValidator implements Validator {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
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

            return; //Нет смысла проводить валидацию списка прав доступа, если запрос уже не валиден
        }

        if (roleCreationRequest.permissions() != null && !roleCreationRequest.permissions().isEmpty())
            this.validateInputPermissions(roleCreationRequest.permissions(), errors);

    }

    public void validateInputPermissions(@NonNull Set<String> permissions, @NonNull Errors errors) {
        Set<String> existingPermissions = permissionRepository.findAllByPermissionIn(permissions).stream()
                .map(Permission::getPermission)
                .collect(Collectors.toSet());

        for (String permission : permissions) {
            if (!existingPermissions.contains(permission)) {
                errors.rejectValue(
                        "permissions",
                        RoleErrorCode.INVALID_PERMISSION_CODE.getCode(),
                        messageSourceHelper.getLocalizedMessage("messages.permission.not-found.permission", permission)
                );

                break;
            }
        }
    }
}
