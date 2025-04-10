package me.stinper.jwtauth.service.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.core.error.PermissionErrorCode;
import me.stinper.jwtauth.dto.permission.PermissionCreationRequest;
import me.stinper.jwtauth.dto.permission.PermissionDto;
import me.stinper.jwtauth.entity.Permission;
import me.stinper.jwtauth.exception.EntityValidationException;
import me.stinper.jwtauth.exception.NoSuchPropertyException;
import me.stinper.jwtauth.exception.RelatedEntityExistsException;
import me.stinper.jwtauth.exception.ResourceNotFoundException;
import me.stinper.jwtauth.mapping.PermissionMapper;
import me.stinper.jwtauth.repository.PermissionRepository;
import me.stinper.jwtauth.repository.RoleRepository;
import me.stinper.jwtauth.service.entity.contract.PermissionService;
import me.stinper.jwtauth.utils.LoggingUtils;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import me.stinper.jwtauth.validation.PermissionCreationValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.util.Collections;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionCreationValidator permissionCreationValidator;
    private final PermissionMapper permissionMapper;
    private final MessageSourceHelper messageSourceHelper;

    @Override
    public Page<PermissionDto> findAll(@NonNull Pageable pageable) {
        try {
            log.atDebug().log(() -> "[#findAll]: Начало выполнения метода. Запрос на пагинацию: " + pageable.toString());

            Page<PermissionDto> permissions = permissionRepository.findAll(pageable)
                    .map(permissionMapper::toPermissionDto);

            log.atDebug().log(
                    "[#findAll]: Выполнение метода завершено. Всего сущностей: {}, всего страниц: {}",
                    permissions.getTotalElements(),
                    permissions.getTotalPages()
            );

            return permissions;
        }
        catch (PropertyReferenceException pre) {
            log.atWarn().log("[#findAll]: Свойство с именем '{}' не существует", pre.getPropertyName());
            throw new NoSuchPropertyException(pre);
        }
    }

    @Override
    public Optional<PermissionDto> findById(@NonNull Long id) {
        log.atDebug().log("[#findById]: Начало выполнения метода. ID: {}", id);

        Optional<PermissionDto> permissionDto = permissionRepository.findById(id)
                .map(permissionMapper::toPermissionDto);

        permissionDto.ifPresentOrElse(
                (p) -> log.atDebug().log("[#findById]: Право доступа с ID '{}' найдено: {}", id, p),
                () -> log.atDebug().log("[#findById]: Право доступа с ID '{}' не найдено", id)
        );

        return permissionDto;
    }

    @Override
    @Transactional
    public PermissionDto create(@NonNull PermissionCreationRequest permissionCreationRequest) {
        log.atDebug().log(() -> "[#create]: Начало выполнения метода. Информация о запросе: " + permissionCreationRequest);
        Errors permissionCreationErrors = permissionCreationValidator.validateObject(permissionCreationRequest);

        if (permissionCreationErrors.hasFieldErrors()) {
            log.atDebug().log(() -> "[#create]: Ошибка валидации запроса на создание права доступа. \n\tСписок ошибок:\n"
                    + LoggingUtils.logFieldErrorsListLineSeparated(permissionCreationErrors.getFieldErrors()));

            throw new EntityValidationException(permissionCreationErrors.getFieldErrors());
        }

        log.atDebug().log("[#create]: Валидация запроса на создание права доступа успешно пройдена");

        Permission permission = permissionMapper.toPermission(permissionCreationRequest);
        permission = permissionRepository.save(permission);

        log.atInfo().log("[#create]: Право доступа с именем '{}' успешно создано в БД", permission.getPermission());

        return permissionMapper.toPermissionDto(permission);
    }

    @Override
    @Transactional
    public PermissionDto updateDescription(@NonNull Long id, @NonNull String description) {
        log.atDebug().log("[#updateDescription]: Начало выполнения метода. Попытка изменить описание права доступа с ID '{}' на '{}'",
                id, description);

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(messageSourceHelper.getLocalizedMessage("messages.permission.not-found.id", id))
                );

        permission.setDescription(description);
        permission = permissionRepository.save(permission);

        log.atInfo().log("[#updateDescription]: Описание права доступа '{}' было изменено на '{}'", permission.getPermission(), description);

        return permissionMapper.toPermissionDto(permission);
    }

    @Override
    @Transactional
    public void delete(@NonNull Long id) throws RelatedEntityExistsException {
        log.atDebug().log("[#delete]: Начало выполнения метода. Попытка удаления права доступа с ID: '{}'", id);

        if (this.isRelatedEntitiesExist(id)) {
            log.atWarn().log("[#delete]: Невозможно удалить право доступа с ID '{}', т.к. для него существуют связанные сущности", id);

            throw new RelatedEntityExistsException(
                    PermissionErrorCode.RELATED_ROLE_EXISTS.getCode(),
                    "messages.permission.integrity.related-role-exists"
            );
        }

        permissionRepository.deleteById(id);

        log.atInfo().log("[#delete]: Право доступа с ID '{}' было удалено из БД", id);
    }

    private boolean isRelatedEntitiesExist(@NonNull Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId).orElse(null);

        if (permission != null)
            return roleRepository.existsByPermissions(Collections.singletonList(permission));

        return false;
    }
}
