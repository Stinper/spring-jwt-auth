package me.stinper.jwtauth.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на вход")
public record LoginRequest(

        @Schema(
                title = "Электронная почта",
                description = "Электронная почта, указанная при регистрации учетной записи",
                example = "user@gmail.com"
        )
        @NotBlank(message = "{messages.user.validation.fields.email.blank}")
        @Email(message = "{messages.user.validation.fields.email.incorrect-pattern}")
        String email,

        @Schema(
                title = "Пароль",
                description = "Пароль от учетной записи",
                example = "mYVerySecuRedPasSwoRD"
        )
        @NotBlank(message = "{messages.user.validation.fields.password.blank}")
        String password
) {}
