package me.stinper.jwtauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Объект запроса, который представляет собой запрос на обновление access токена по ранее выданному refresh токену
 * @param refreshToken Ранее полученный refresh токен
 */
@Schema(description = "Запрос на обновление Access-токена")
public record RefreshAccessTokenRequest(

        @Schema(
                title = "Refresh-токен",
                description = "Refresh-токен, который будет использован для генерации нового Access-токена",
                type = "string",
                example = "eyJhbGciOiJSUz..."
        )
        @JsonProperty(value = "refresh_token")
        @NotBlank(message = "{messages.tokens.validation.refresh-access-token-request.blank}")
        String refreshToken
) {}
