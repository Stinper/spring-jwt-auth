package me.stinper.jwtauth.dto.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на создание роли")
public record RoleCreationRequest(

        @Schema(
                title = "Имя роли",
                description = "Уникальное имя создаваемой роли",
                example = "ROLE_MY_CUSTOM_ROLE",
                pattern = ROLE_NAME_PATTERN
        )
        @JsonProperty(value = "role_name")
        @NotBlank(message = "{messages.role.validation.fields.role-name.blank}")
        @Pattern(regexp = ROLE_NAME_PATTERN, message = "{messages.role.validation.fields.role-name.incorrect-pattern}")
        String roleName

) {
        public static final String ROLE_NAME_PATTERN = "(?i)^ROLE_\\w+";
}
