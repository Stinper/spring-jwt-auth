package me.stinper.jwtauth.dto.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import me.stinper.jwtauth.entity.Role;

import java.util.Set;

@Builder
@Schema(description = "Запрос на создание роли")
public record RoleCreationRequest(

        @Schema(
                title = "Имя роли",
                description = "Уникальное имя создаваемой роли",
                example = "ROLE_MY_CUSTOM_ROLE",
                pattern = ROLE_NAME_PATTERN,
                minLength = 6, //Обязательный префикс ROLE_ + хотя бы 1 символ = 6 символов
                maxLength = Role.Constraints.ROLE_NAME_FIELD_MAX_LENGTH
        )
        @JsonProperty(value = "role_name")
        @NotBlank(message = "{messages.role.validation.fields.role-name.blank}")
        @Pattern(regexp = ROLE_NAME_PATTERN, message = "{messages.role.validation.fields.role-name.incorrect-pattern}")
        @Size(min = 6, message = "{messages.role.validation.fields.role-name.too-short}")
        @Size(max = 255, message = "{messages.role.validation.fields.role-name.too-long}")
        String roleName,

        @Schema(
                title = "Префикс роли",
                description = "Название роли, предназначенное для чтения человеком",
                example = "Администратор",
                minLength = 1,
                maxLength = Role.Constraints.PREFIX_FIELD_MAX_LENGTH
        )
        @Size(min = 1, message = "{messages.role.validation.fields.prefix.too-short}")
        @Size(max = 255, message = "{messages.role.validation.fields.prefix.too-long}")
        @NotBlank(message = "{messages.role.validation.fields.prefix.blank}")
        String prefix,

        @Schema(
                title = "Права доступа",
                description = "Права доступа, принадлежащие к этой роли. Список должен содержать названия прав доступа",
                example = "[user.read.find-all, user.create.create-user]"
        )
        Set<String> permissions
) {
        public static final String ROLE_NAME_PATTERN = "(?i)^ROLE_\\w+";
}
