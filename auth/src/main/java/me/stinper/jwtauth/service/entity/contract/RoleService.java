package me.stinper.jwtauth.service.entity.contract;

import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.dto.role.RoleDto;
import me.stinper.jwtauth.exception.RelatedEntityExistsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * Сервис, содержащий операции с моделью роли (Role)
 * @see me.stinper.jwtauth.entity.Role
 * @see RoleDto
 * @see RoleCreationRequest
 */
public interface RoleService {
    Page<RoleDto> findAll(Pageable pageable);

    Optional<RoleDto> findByName(@NonNull String name);

    /**
     * Создает новую роль в базе данных
     * @param roleCreationRequest объект, содержащий информацию о роли, которую необходимо создать
     * @return информация о созданной роли, преобразованная в {@link RoleDto}
     * @see RoleCreationRequest
     */
    RoleDto create(@NonNull RoleCreationRequest roleCreationRequest);

    /**
     * Удалять роль с заданным именем из базы данных. Для обеспечения целостности данных,
     * метод проверяет наличие связанных сущностей (пользователей) с этой ролью перед ее удалением
     * @param name имя роли
     * @throws RelatedEntityExistsException если существует хотя бы один пользователь, связанный с этой ролью
     */
    void delete(@NonNull String name) throws RelatedEntityExistsException;
}
