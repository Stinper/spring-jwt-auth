package me.stinper.jwtauth.service.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.core.error.RoleErrorCode;
import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.dto.role.RoleDto;
import me.stinper.jwtauth.dto.role.RolePermissionUpdateRequest;
import me.stinper.jwtauth.entity.Permission;
import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.exception.EntityValidationException;
import me.stinper.jwtauth.exception.NoSuchPropertyException;
import me.stinper.jwtauth.exception.RelatedEntityExistsException;
import me.stinper.jwtauth.exception.ResourceNotFoundException;
import me.stinper.jwtauth.mapping.RoleMapper;
import me.stinper.jwtauth.repository.PermissionRepository;
import me.stinper.jwtauth.repository.RoleRepository;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.service.entity.contract.RoleService;
import me.stinper.jwtauth.utils.LoggingUtils;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import me.stinper.jwtauth.validation.RoleCreationValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final RoleCreationValidator roleCreationValidator;
    private final MessageSourceHelper messageSourceHelper;

    @Override
    public Page<RoleDto> findAll(Pageable pageable) {
        try {
            log.atDebug().log(() -> "[#findAll]: Начало выполнения метода. Запрос на пагинацию: " + pageable.toString());

            Page<RoleDto> roles = roleRepository.findAll(pageable)
                    .map(roleMapper::toRoleDto);

            log.atDebug().log(
                    "[#findAll]: Выполнение метода завершено. Всего ролей: {}, всего страниц: {}",
                    roles.getTotalElements(),
                    roles.getTotalPages()
            );

            return roles;
        }
        catch (PropertyReferenceException pre) {
            log.atWarn().log("[#findAll]: Свойство с именем '{}' не существует", pre.getPropertyName());
            throw new NoSuchPropertyException(pre);
        }
    }

    @Override
    public Optional<RoleDto> findByName(@NonNull String name) {
        log.atDebug().log("[#findByName]: Начало выполнения метода. Имя роли: {}", name);

        Optional<RoleDto> role = roleRepository.findByRoleNameIgnoreCase(name)
                .map(roleMapper::toRoleDto);

        role.ifPresentOrElse(
                (presentedRole) -> log.atDebug().log("[#findByName]: Роль с именем {} найдена: {}", name, role),
                () -> log.atDebug().log("[#findByName]: Роль с именем '{}' не найдена", name)
        );

        return role;
    }

    @Override
    public RoleDto create(@NonNull RoleCreationRequest roleCreationRequest) {
        log.atDebug().log(() -> "[#create]: Начало выполнения метода. Информация о запросе: " + roleCreationRequest);
        Errors roleCreationErrors = roleCreationValidator.validateObject(roleCreationRequest);

        if (roleCreationErrors.hasFieldErrors()) {
            log.atDebug().log(() -> "[#create]: Ошибка валидации запроса на создание роли. \n\tСписок ошибок: \n"
                    + LoggingUtils.logFieldErrorsListLineSeparated(roleCreationErrors.getFieldErrors()));

            throw new EntityValidationException(roleCreationErrors.getFieldErrors());
        }

        log.atDebug().log("[#create]: Валидация запроса на создания роли успешно пройдена");

        Role role = roleMapper.toRole(roleCreationRequest);
        role = roleRepository.save(role);

        log.atInfo().log("[#create]: Роль с именем '{}' успешно создана в БД", role.getRoleName());

        return roleMapper.toRoleDto(role);
    }

    @Override
    public RoleDto updatePermissions(@NonNull String roleName, @NonNull RolePermissionUpdateRequest permissionUpdateRequest) {
        log.atDebug().log("[#updatePermissions]: Попытка изменения прав доступа для роли '{}'", roleName);

        Role role = roleRepository.findByRoleNameIgnoreCase(roleName)
                .orElseThrow(() -> {
                    log.atDebug().log("[#updatePermissions]: Роль с именем '{}' не существует", roleName);

                    return new ResourceNotFoundException(
                            messageSourceHelper.getLocalizedMessage("messages.role.not-found.role-name", roleName));
                });

        Errors permissionListValidationErrors = new SimpleErrors(permissionUpdateRequest);
        roleCreationValidator.validateInputPermissions(permissionUpdateRequest.permissions(), permissionListValidationErrors);

        if (permissionListValidationErrors.hasErrors()) {
            log.atDebug().log(() -> "[#updatePermissions]: Обнаружены ошибки в списке ролей. \n\tСписок ошибок: \n"
                    + LoggingUtils.logFieldErrorsListLineSeparated(permissionListValidationErrors.getFieldErrors())
            );

            throw new EntityValidationException(permissionListValidationErrors.getFieldErrors());
        }

        List<Permission> permissionList = permissionRepository.findAllByPermissionIn(permissionUpdateRequest.permissions());
        role.setPermissions(permissionList);
        role = roleRepository.save(role);

        log.atInfo().log("[#updatePermissions]: Для роли '{}' обновлен список прав доступа. \n\tНовый список прав доступа: {}",
                roleName, permissionUpdateRequest.permissions()
        );

        return roleMapper.toRoleDto(role);
    }

    @Override
    @Transactional
    public void delete(@NonNull String name) {
        log.atDebug().log("[#delete]: Начало выполнения метода. Имя роли для удаления: {}", name);

        if(this.isRelatedUserExists(name)) {
            log.atWarn().log("[#delete]: Невозможно удалить роль с именем '{}', т.к. для нее существуют связанные сущности", name);

            throw new RelatedEntityExistsException(
                    RoleErrorCode.RELATED_USER_EXISTS.getCode(),
                    "messages.role.integrity.related-user-exists"
            );
        }

        roleRepository.deleteByRoleNameIgnoreCase(name);

        log.atInfo().log("[#delete]: Роль с именем '{}' успешно удалена", name);
    }

    private boolean isRelatedUserExists(@NonNull String roleName) {
        Role role = roleRepository.findByRoleNameIgnoreCase(roleName).orElse(null);

        if (role == null)
            return false;

        return userRepository.existsByRoles(Collections.singletonList(role));
    }
}
