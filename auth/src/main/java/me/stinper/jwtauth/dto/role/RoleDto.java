package me.stinper.jwtauth.dto.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import me.stinper.jwtauth.dto.permission.PermissionDto;

import java.util.List;

@Builder
@Schema(description = "Объект, содержащий информацию о конкретной роли")
public record RoleDto(

        @Schema(
                title = "Идентификатор роли",
                description = "Уникальный идентификатор роли в числовом формате",
                example = "1"
        )
        Long id,

        @Schema(
                title = "Имя роли",
                description = "Уникальное имя роли",
                example = "ROLE_ADMIN"
        )
        @JsonProperty(value = "role_name")
        String roleName,

        @Schema(
                title = "Префикс роли",
                description = "Название роли, предназначенное для чтения человеком",
                example = "Администратор"
        )
        String prefix,

        @Schema(
                title = "Права доступа",
                description = "Права доступа, принадлежащие к этой роли"
        )
        List<PermissionDto> permissions
) {}
