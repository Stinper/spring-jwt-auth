package me.stinper.jwtauth.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@Schema(description = "Запрос на обновления списка прав доступа конкретной роли")
public record RolePermissionUpdateRequest(

        @Schema(
                title = "Список прав доступа",
                description = "Новый список прав доступа для роли. Указывается список кодов прав доступа",
                example = "[user.read.find-all-users, user.create.create-user]"
        )
        @NotNull(message = "{messages.role.update.validation.permissions-list.null}")
        Set<String> permissions
) {}
