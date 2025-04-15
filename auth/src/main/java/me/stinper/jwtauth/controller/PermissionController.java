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
import me.stinper.jwtauth.dto.permission.PermissionCreationRequest;
import me.stinper.jwtauth.dto.permission.PermissionDescriptionUpdateRequest;
import me.stinper.jwtauth.dto.permission.PermissionDto;
import me.stinper.jwtauth.exception.RelatedEntityExistsException;
import me.stinper.jwtauth.service.entity.contract.PermissionService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Set;

@Tag(
        name = "Операции с правами доступа"
)
@RestController
@RequestMapping("/api/v1/jwt-auth/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;
    private final jakarta.validation.Validator validator;

    @GetMapping
    @OperationPermission(
            permission = "permission.read.find-all-permissions",
            description = "Пользователь с этим правом может получать список всех существующих в системе прав доступа"
    )
    @PreAuthorize("@permissionSecurityService.isAllowedToFindAllPermissions(principal)")
    @Operation(
            summary = "Получение всех прав доступа",
            description = "Предназначен для получения всех прав доступа в постраничном формате, т.е. с использованием пагинации",
            responses = {
                    @ApiResponse(responseCode = "200", ref = "PagedOK")
            },
            parameters = {
                    @Parameter(ref = "Authorization")
            }
    )
    public ResponseEntity<Page<PermissionDto>> findAll(@ModelAttribute @ParameterObject EntityPaginationRequest entityPaginationRequest) {
        Set<ConstraintViolation<EntityPaginationRequest>> constraintViolations = this.validator.validate(entityPaginationRequest);

        if (!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);

        return ResponseEntity.ok(
                permissionService.findAll(entityPaginationRequest.buildPageableFromRequest())
        );
    }


    @GetMapping("/{id}")
    @OperationPermission(
            permission = "permission.read.find-by-id",
            description = "Пользователь с этим правом может получить информацию о конкретном праве доступа по его идентификатору"
    )
    @PreAuthorize("@permissionSecurityService.isAllowedToFindPermissionById(#id, principal)")
    @Operation(
            summary = "Получение права доступа по идентификатору",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Право доступа успешно найдено",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PermissionDto.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", ref = "NotFound")
            },
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Уникальный идентификатор права доступа",
                            example = "1",
                            required = true,
                            in = ParameterIn.PATH
                    ),
                    @Parameter(ref = "Authorization")
            }
    )
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return permissionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    @OperationPermission(
            permission = "permission.create.create-permission",
            description = "Пользователь с этим правом может создать новое право доступа"
    )
    @PreAuthorize("@permissionSecurityService.isAllowedToCreatePermission(principal)")
    @Operation(
            summary = "Создание права доступа",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные, необходимые для создания права доступа",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PermissionCreationRequest.class),
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
    public ResponseEntity<PermissionDto> create(@RequestBody PermissionCreationRequest permissionCreationRequest) {
        Set<ConstraintViolation<PermissionCreationRequest>> constraintViolations = this.validator.validate(permissionCreationRequest);

        if (!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);

        PermissionDto permissionDto = permissionService.create(permissionCreationRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(permissionDto.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(permissionDto);
    }


    @PatchMapping("/{id}/description")
    @OperationPermission(
            permission = "permission.update.description",
            description = "Пользователь с этим правом может обновлять описание права доступа по его идентификатору"
    )
    @PreAuthorize("@permissionSecurityService.isAllowedToUpdatePermissionDescription(#id, principal)")
    @Operation(
            summary = "Обновление описания права доступа",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новое описание права доступа",
                    required = true,
                    content = @Content(
                            schema = @Schema(type = "string"),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Описание успешно изменено",
                            content = @Content(
                                    schema = @Schema(implementation = PermissionDto.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(responseCode = "404", ref = "NotFound")
            },
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Уникальный идентификатор права доступа",
                            example = "1",
                            required = true,
                            in = ParameterIn.PATH
                    ),
                    @Parameter(ref = "Authorization")
            }
    )
    public ResponseEntity<PermissionDto> updateDescription(@PathVariable Long id,
                                                           @RequestBody PermissionDescriptionUpdateRequest descriptionUpdateRequest) {
        Set<ConstraintViolation<PermissionDescriptionUpdateRequest>> constraintViolations = this.validator.validate(descriptionUpdateRequest);

        if (!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);

        return ResponseEntity.ok(
                permissionService.updateDescription(id, descriptionUpdateRequest.description())
        );
    }


    @DeleteMapping("/{id}")
    @OperationPermission(
            permission = "permission.delete.delete-by-id",
            description = "Пользователь с этим правом может удалить право доступа по его идентификатору"
    )
    @PreAuthorize("@permissionSecurityService.isAllowedToDeletePermission(#id, principal)")
    @Operation(
            summary = "Удаление права доступа по идентификатору",
            responses = {
                    @ApiResponse(responseCode = "201", ref = "NoContent"),
                    @ApiResponse(
                            responseCode = "409", // Conflict
                            description = "Если удаляемое право доступа связано хотя бы с 1 ролью",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            },
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Уникальный идентификатор права доступа",
                            example = "1",
                            required = true,
                            in = ParameterIn.PATH
                    ),
                    @Parameter(ref = "Authorization")
            }
    )
    public ResponseEntity<?> delete(@PathVariable Long id) throws RelatedEntityExistsException {
        permissionService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
