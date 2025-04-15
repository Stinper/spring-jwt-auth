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
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.core.security.permission.annotation.OperationPermission;
import me.stinper.jwtauth.core.security.permission.annotation.Permissions;
import me.stinper.jwtauth.dto.EntityPaginationRequest;
import me.stinper.jwtauth.dto.user.PasswordChangeRequest;
import me.stinper.jwtauth.dto.user.UserCreationRequest;
import me.stinper.jwtauth.dto.user.UserDto;
import me.stinper.jwtauth.service.entity.contract.UserPasswordService;
import me.stinper.jwtauth.service.entity.contract.UserService;
import me.stinper.jwtauth.service.security.contract.UserSecurityService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

@Tag(
        name = "Операции с пользователями"
)
@RestController
@RequestMapping("/api/v1/jwt-auth/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserPasswordService userPasswordService;
    private final UserSecurityService userSecurityService;
    private final jakarta.validation.Validator validator;

    @GetMapping
    @Permissions(permissions = {
            @OperationPermission(
                    permission = "user.read.find-all-users",
                    description = "Пользователь с этим правом может просматривать список всех не деактивированных пользователей"
            ),
            @OperationPermission(
                    permission = "user.read.read-deactivated-users",
                    description = "Пользователь с этим правом может просматривать тех пользователей, чья учетная запись была деактивирована"
            )
    })
    @PreAuthorize("@userSecurityService.isAllowedToFindAllUsers(principal)")
    @Operation(
            summary = "Получение всех пользователей",
            description = "Предназначен для получения всех пользователей в постраничном формате, т.е. с использованием пагинации",
            responses = {
                    @ApiResponse(responseCode = "200", ref = "PagedOK")
            },
            parameters = {
                    @Parameter(ref = "Authorization")
            }
    )
    public ResponseEntity<Page<UserDto>> findAll(@ModelAttribute @ParameterObject EntityPaginationRequest entityPaginationRequest,
                                                 @AuthenticationPrincipal JwtAuthUserDetails user) {
        Set<ConstraintViolation<EntityPaginationRequest>> constraintViolations = this.validator.validate(entityPaginationRequest);

        if (!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);

        return ResponseEntity.ok(
                userService.findAll(entityPaginationRequest.buildPageableFromRequest(), userSecurityService.chooseUserFilterStrategy(user))
        );
    }



    @GetMapping("/{uuid}")
    @OperationPermission(
            permission = "user.read.find-by-uuid",
            description = "Пользователь с этим правом может получить пользователя по его идентификатору"
    )
    @PreAuthorize("@userSecurityService.isAllowedToFindUserByUUID(#uuid, principal)")
    @Operation(
            summary = "Получение пользователя по UUID",
            description = "Предназначен для получения одного пользователя по его уникальному идентификатору (UUID)",
            parameters = {
                    @Parameter(
                            name = "uuid",
                            description = "Уникальный идентификатор пользователя, которого необходимо получить",
                            example = "01955bac-754c-7d9c-887d-76e2426ddd71",
                            required = true,
                            in = ParameterIn.PATH
                    ),
                    @Parameter(ref = "Authorization")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Пользователь успешно найден",
                            content = @Content(
                                    schema = @Schema(implementation = UserDto.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(responseCode = "404", ref = "NotFound")
            }
    )
    public ResponseEntity<UserDto> findById(@PathVariable UUID uuid) {
        return userService.findByUUID(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    @PostMapping
    @Operation(
            summary = "Создание пользователя",
            description = "Предназначен для создания (регистрации) нового пользователя",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные, необходимые для создания нового пользователя",
                    content = @Content(
                            schema = @Schema(implementation = UserCreationRequest.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    ),
                    required = true
            ),
            responses = {
                @ApiResponse(responseCode = "201", ref = "Created")
            }
    )
    public ResponseEntity<?> create(@RequestBody UserCreationRequest userCreationRequest) {
        Set<ConstraintViolation<UserCreationRequest>> constraintViolations = this.validator.validate(userCreationRequest);

        if (!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);

        UserDto user = userService.create(userCreationRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(user);
    }



    @DeleteMapping("/{uuid}")
    @OperationPermission(
            permission = "user.delete.deactivate-by-uuid",
            description = "Пользователь с этим правом может деактивировать учетную запись пользователя по его идентификатору"
    )
    @PreAuthorize("@userSecurityService.isAllowedToDeleteAccount(#uuid, principal)")
    @Operation(
            summary = "Удаление пользователя",
            description =
                    """
                    Предназначен для "мягкого" удаления (деактивации) учетной записи пользователя. Результатом выполнения
                    этой операции будет то, что пользователь больше не сможет авторизоваться в системе, т.е. получить
                    новый токен, или использовать старые, хотя его учетная запись все еще будет оставаться в БД
                    """,
            parameters = {
                    @Parameter(
                            name = "uuid",
                            description = "Уникальный идентификатор пользователя, которого необходимо получить",
                            example = "01955bac-754c-7d9c-887d-76e2426ddd71",
                            required = true,
                            in = ParameterIn.PATH
                    ),
                    @Parameter(ref = "Authorization")
            },
            responses = {
                @ApiResponse(responseCode = "204", ref = "NoContent")
            }
    )
    public ResponseEntity<?> deleteByUUID(@PathVariable UUID uuid) {
        userService.deleteByUUID(uuid);
        return ResponseEntity.noContent().build();
    }



    @PatchMapping("/password")
    @Operation(
            summary = "Смена пароля",
            description = "Предназначен для смены пароля от учетной записи",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные, необходимые для смены пароля",
                    content = @Content(
                            schema = @Schema(implementation = PasswordChangeRequest.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    ),
                    required = true
            ),
            responses = {
                @ApiResponse(responseCode = "204", ref = "NoContent")
            },
            parameters = {
                    @Parameter(ref = "Authorization")
            }
    )
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest passwordChangeRequest,
                                            @AuthenticationPrincipal JwtAuthUserDetails userDetails) {
        Set<ConstraintViolation<PasswordChangeRequest>> constraintViolations = this.validator.validate(passwordChangeRequest);

        if (!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);

        userPasswordService.changePassword(passwordChangeRequest, userDetails);

        return ResponseEntity.noContent().build();
    }
}
