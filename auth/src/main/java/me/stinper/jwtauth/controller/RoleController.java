package me.stinper.jwtauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.swagger.AuthorizationHeaderDescription;
import me.stinper.jwtauth.dto.EntityPaginationRequest;
import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.dto.role.RoleDto;
import me.stinper.jwtauth.exception.RelatedEntityExistsException;
import me.stinper.jwtauth.service.entity.contract.RoleService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@Validated
public class RoleController {
    private final RoleService roleService;
    private final jakarta.validation.Validator validator;


    @GetMapping
    @Operation(
            summary = "Получение всех ролей",
            description =
                    """
                    Предназначен для получения всех ролей в постраничном формате, т.е. с использованием пагинации
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное выполнение операции",
                            content = @Content(
                                    schema = @Schema(implementation = PagedModel.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            }
    )
    @AuthorizationHeaderDescription
    public ResponseEntity<Page<RoleDto>> findAll(@ModelAttribute @ParameterObject EntityPaginationRequest entityPaginationRequest) {
        Set<ConstraintViolation<EntityPaginationRequest>> constraintViolations = this.validator.validate(entityPaginationRequest);

        if (!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);

        return ResponseEntity.ok(roleService.findAll(entityPaginationRequest.buildPageableFromRequest()));
    }


    @GetMapping("/{roleName}")
    @Operation(
            summary = "Получение роли по уникальному имени",
            description =
                    """
                    Предназначен для получения одной роли по ее уникальному имени
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Роль успешно найдена",
                            content = @Content(
                                    schema = @Schema(implementation = RoleDto.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Роль с таким именем не найдена",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            },
            parameters = {
                    @Parameter(
                            name = "roleName",
                            description = "Уникальное имя роли, которую необходимо получить",
                            example = "ROLE_ADMIN",
                            required = true,
                            in = ParameterIn.PATH
                    )
            }
    )
    @AuthorizationHeaderDescription
    public ResponseEntity<RoleDto> findByRoleName(@PathVariable String roleName) {
        return roleService.findByName(roleName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    @Operation(
            summary = "Создание роли",
            description =
                    """
                    Предназначен для создания новой роли
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные, необходимые для создания роли",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RoleCreationRequest.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201", //Created
                            description = "Роль была успешно создана",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            ),
                            headers = {
                                    @Header(
                                            name = "Location",
                                            description = "Расположение (URI) только что созданной роли"
                                    )
                            }
                    )
            }
    )
    @AuthorizationHeaderDescription
    public ResponseEntity<?> create(@RequestBody RoleCreationRequest roleCreationRequest) {
        Set<ConstraintViolation<RoleCreationRequest>> constraintViolations = this.validator.validate(roleCreationRequest);

        if (!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);

        RoleDto role = roleService.create(roleCreationRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{roleName}")
                .buildAndExpand(role.roleName())
                .toUri();

        return ResponseEntity.created(location).build();
    }


    @DeleteMapping("/{roleName}")
    @Operation(
            summary = "Удаление роли",
            description =
                    """
                    Предназнчен для удаление роли по заданному уникальному имени
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "204", //No Content
                            description = "Роль была успешно удалена",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
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
                    )
            }
    )
    @AuthorizationHeaderDescription
    public ResponseEntity<?> delete(@PathVariable String roleName) throws RelatedEntityExistsException {
        roleService.delete(roleName);

        return ResponseEntity.noContent().build();
    }

}
