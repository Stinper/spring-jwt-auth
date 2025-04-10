package me.stinper.jwtauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.security.permission.annotation.OperationPermission;
import me.stinper.jwtauth.dto.EntityPaginationRequest;
import me.stinper.jwtauth.dto.role.RolePermissionUpdateRequest;
import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.dto.role.RoleDto;
import me.stinper.jwtauth.exception.RelatedEntityExistsException;
import me.stinper.jwtauth.service.entity.contract.RoleService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Set;

@Tag(
        name = "Операции с ролями"
)
@RestController
@RequestMapping("/api/v1/jwt-auth/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    private final jakarta.validation.Validator validator;


    @GetMapping
    @OperationPermission(
            permission = "role.read.find-all-roles",
            description = "Пользователь с этим правом может просматривать список всех ролей, существующих в системе"
    )
    @Operation(
            summary = "Получение всех ролей",
            description = "Предназначен для получения всех ролей в постраничном формате, т.е. с использованием пагинации",
            responses = {
                    @ApiResponse(responseCode = "200", ref = "PagedOK")
            },
            parameters = {
                    @Parameter(ref = "Authorization")
            }
    )
    public ResponseEntity<Page<RoleDto>> findAll(@ModelAttribute @ParameterObject EntityPaginationRequest entityPaginationRequest) {
        Set<ConstraintViolation<EntityPaginationRequest>> constraintViolations = this.validator.validate(entityPaginationRequest);

        if (!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);

        return ResponseEntity.ok(roleService.findAll(entityPaginationRequest.buildPageableFromRequest()));
    }


    @GetMapping("/{roleName}")
    @OperationPermission(
            permission = "role.read.find-role-by-name",
            description = "Пользователь с этим правом может информацию о роли по ее имени"
    )
    @Operation(
            summary = "Получение роли по уникальному имени",
            description = "Предназначен для получения одной роли по ее уникальному имени",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Роль успешно найдена",
                            content = @Content(
                                    schema = @Schema(implementation = RoleDto.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(responseCode = "404", ref = "NotFound")
            },
            parameters = {
                    @Parameter(
                            name = "roleName",
                            description = "Уникальное имя роли, которую необходимо получить",
                            example = "ROLE_ADMIN",
                            required = true,
                            in = ParameterIn.PATH
                    ),
                    @Parameter(ref = "Authorization")
            }
    )
    public ResponseEntity<RoleDto> findByRoleName(@PathVariable String roleName) {
        return roleService.findByName(roleName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    @OperationPermission(
            permission = "role.create.create-role",
            description = "Пользователь с этим правом может создавать новую роль в системе"
    )
    @Operation(
            summary = "Создание роли",
            description = "Предназначен для создания новой роли",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные, необходимые для создания роли",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RoleCreationRequest.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", ref = "Created")
            },
            parameters = {
                    @Parameter(ref = "Authorization")
            }
    )
    public ResponseEntity<RoleDto> create(@RequestBody RoleCreationRequest roleCreationRequest) {
        Set<ConstraintViolation<RoleCreationRequest>> constraintViolations = this.validator.validate(roleCreationRequest);

        if (!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);

        RoleDto role = roleService.create(roleCreationRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{roleName}")
                .buildAndExpand(role.roleName())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(role);
    }



    @PatchMapping("/{roleName}/permissions")
    @OperationPermission(
            permission = "role.update.partial.permissions-list",
            description = "Пользователь с этим правом может обновлять список прав доступа для роли по ее идентификатору"
    )
    @Operation(
            summary = "Обновление списка прав доступа",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новый список прав доступа",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RolePermissionUpdateRequest.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Список прав доступа успешно обновлен",
                            content = @Content(
                                    schema = @Schema(implementation = RoleDto.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(responseCode = "404", ref = "NotFound")
            },
            parameters = {
                    @Parameter(
                            name = "roleName",
                            description = "Уникальное имя роли",
                            example = "ROLE_ADMIN",
                            required = true,
                            in = ParameterIn.PATH
                    ),
                    @Parameter(ref = "Authorization")
            }
    )
    public ResponseEntity<RoleDto> updatePermissions(@PathVariable String roleName, @RequestBody RolePermissionUpdateRequest permissionUpdateRequest) {
        Set<ConstraintViolation<RolePermissionUpdateRequest>> constraintViolations = validator.validate(permissionUpdateRequest);

        if (!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);

        return ResponseEntity.ok(
                roleService.updatePermissions(roleName, permissionUpdateRequest)
        );
    }


    @DeleteMapping("/{roleName}")
    @OperationPermission(
            permission = "role.delete.delete-role-by-name",
            description = "Пользователь с этим правом может удалять роли"
    )
    @Operation(
            summary = "Удаление роли",
            description = "Предназначен для удаления роли по заданному уникальному имени",
            responses = {
                    @ApiResponse(responseCode = "204", ref = "NoContent"),
                    @ApiResponse(
                            responseCode = "409", // Conflict
                            description = "Если удаляемая роль связана хотя бы с 1 пользователем",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            },
            parameters = {
                    @Parameter(
                            name = "roleName",
                            description = "Уникальное имя роли, которую необходимо удалить",
                            example = "ROLE_ADMIN",
                            required = true,
                            in = ParameterIn.PATH
                    ),
                    @Parameter(ref = "Authorization")
            }
    )
    public ResponseEntity<?> delete(@PathVariable String roleName) throws RelatedEntityExistsException {
        roleService.delete(roleName);

        return ResponseEntity.noContent().build();
    }

}
