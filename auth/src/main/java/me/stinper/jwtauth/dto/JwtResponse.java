package me.stinper.jwtauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Объект ответа, в котором содержатся токены
 */
@Schema(description = "Ответ при успешном запросе на вход")
public record JwtResponse(
        @Schema(
                title = "Access-токен",
                description = "Представляет собой Access-токен, который используется для доступа к защищенным эндпоинтам",
                type = "string",
                example = "eyJhbGciOiJSUz..."
        )
        @JsonProperty(value = "access_token")
        String accessToken,

        @Schema(
                title = "Refresh-токен",
                description = "Представляет собой Refresh-токен, который используется для получения новых Access-токенов",
                type = "string",
                example = "eyJhbGciOiJSUz..."
        )
        @JsonProperty(value = "refresh_token")
        String refreshToken
) {}
