package me.stinper.jwtauth.service.entity.contract;

import me.stinper.jwtauth.dto.permission.PermissionCreationRequest;
import me.stinper.jwtauth.dto.permission.PermissionDto;
import me.stinper.jwtauth.exception.RelatedEntityExistsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * Сервис, содержащий операции с моделью права доступа (Permission)
 * @see me.stinper.jwtauth.entity.Permission
 * @see PermissionDto
 * @see PermissionCreationRequest
 */
public interface PermissionService {
    Page<PermissionDto> findAll(@NonNull Pageable pageable);

    Optional<PermissionDto> findById(@NonNull Long id);

    PermissionDto create(@NonNull PermissionCreationRequest permissionCreationRequest);

    PermissionDto updateDescription(@NonNull Long id, @NonNull String description);

    /**
     * Удаляет право доступа с заданным ID из базы данных. Для обеспечения целостности данных,
     * метод проверяет наличие связанных сущностей (ролей) с этим правом доступа перед его удалением
     * @param id идентификатор права доступа, которое требуется удалить
     * @throws RelatedEntityExistsException если существует хотя бы одна роль, которой присвоено
     * это право доступа
     */
    void delete(@NonNull Long id) throws RelatedEntityExistsException;
}
