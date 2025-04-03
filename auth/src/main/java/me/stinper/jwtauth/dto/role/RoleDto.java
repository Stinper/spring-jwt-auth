package me.stinper.jwtauth.dto.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

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
        String roleName
) {}
