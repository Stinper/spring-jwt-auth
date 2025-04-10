package me.stinper.jwtauth.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Объект, содержащий информацию о праве доступа")
public record PermissionDto(
        @Schema(
                title = "Идентификатор права доступа",
                description = "Уникальный идентификатор права доступа в числовом формате",
                example = "1"
        )
        Long id,

        @Schema(
                title = "Право доступа",
                description = "Строка, идентифицирующая конкретное право доступа",
                example = "user.create.create-user"
        )
        String permission,

        @Schema(
                title = "Описание",
                description = "Описание права доступа",
                example = "Пользователь с этим правом может создавать новых пользователей в системе"
        )
        String description
) {}
