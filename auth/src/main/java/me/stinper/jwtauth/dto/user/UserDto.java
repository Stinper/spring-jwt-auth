package me.stinper.jwtauth.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import me.stinper.jwtauth.dto.role.RoleDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
@Schema(description = "Объект, содержащий информацию о конкретном пользователе")
public record UserDto(

        @Schema(
                title = "UUID",
                description = "Уникальный идентификатор пользователя в формате UUID",
                example = "01955bac-754c-7d9c-887d-76e2426ddd71"
        )
        UUID uuid,

        @Schema(
                title = "Электронная почта",
                description = "Электронная почта пользователя",
                example = "user@gmail.com"
        )
        String email,

        @Schema(
                title = "Дата и время регистрации",
                description = "Дата и время регистрации пользователя в формате ISO 8601 (UTC)",
                type = "string",
                format = "date-time",
                example = "2024-01-01T12:00:00Z"
        )
        @JsonProperty(value = "registered_at")
        Instant registeredAt,

        @Schema(
                title = "Почта верифицирована",
                description = "Верифицирована ли электронная почта пользователя",
                example = "true"

        )
        @JsonProperty(value = "is_email_verified")
        Boolean isEmailVerified,

        @ArraySchema(
                schema = @Schema(implementation = RoleDto.class),
                arraySchema = @Schema(
                        title = "Роли",
                        description = "Список ролей, присвоенных пользователю"
                )
        )
        List<RoleDto> roles
) {}
