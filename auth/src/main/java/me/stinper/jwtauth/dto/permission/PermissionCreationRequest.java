package me.stinper.jwtauth.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание права доступа")
public record PermissionCreationRequest(
        @Schema(
                title = "Право доступа",
                description = "Строка, идентифицирующая конкретное право доступа",
                example = "user.create.create-user"
        )
        @NotBlank(message = "{messages.permission.validation.fields.permission.blank}")
        @Size(
                max = 255,
                message = "{messages.permission.validation.fields.permission.too-long}"
        )
        String permission,

        @Schema(
                title = "Описание",
                description = "Описание права доступа",
                example = "Пользователь с этим правом может создавать новых пользователей в системе",
                nullable = true
        )
        String description
) {}
