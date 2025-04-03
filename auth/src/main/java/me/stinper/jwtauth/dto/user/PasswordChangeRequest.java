package me.stinper.jwtauth.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import me.stinper.jwtauth.validation.constraints.Password;

@Schema(description = "Объект, содержащий необходимую информацию для смены пароля")
public record PasswordChangeRequest(

        @Schema(
                title = "Старый пароль",
                description = "Старый пароль от учетной записи",
                example = "MyOldPassword123"
        )
        @JsonProperty(value = "old_password")
        @NotBlank(message = "{messages.user.validation.password-change.old-password.blank}")
        String oldPassword,

        @Schema(
                title = "Новый пароль",
                description = "Новый пароль для учетной записи",
                example = "MyNewPassword123"
        )
        @JsonProperty(value = "new_password")
        @NotBlank(message = "{messages.user.validation.password-change.new-password.blank}")
        @Password
        String newPassword,

        @Schema(
                title = "Повторение нового пароля",
                description = "Повторение нового пароля для учетной записи",
                example = "MyNewPassword123"
        )
        @JsonProperty(value = "repeat_new_password")
        @NotBlank(message = "{messages.user.validation.password-change.repeat-new-password.blank}")
        String repeatNewPassword
) {
}
