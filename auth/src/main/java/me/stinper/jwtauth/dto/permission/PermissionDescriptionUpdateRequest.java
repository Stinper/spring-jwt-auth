package me.stinper.jwtauth.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Запрос на обновление описания права доступа")
public record PermissionDescriptionUpdateRequest(

        @Schema(
                title = "Описание",
                description = "Новое описание для права доступа",
                example = "Пользователь с этим правом может просматривать список ролей"
        )
        @NotNull(message = "{messages.permission.update.validation.description.null}")
        String description
) {}
