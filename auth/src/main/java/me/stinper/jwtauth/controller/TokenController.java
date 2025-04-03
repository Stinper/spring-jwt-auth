package me.stinper.jwtauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.dto.JwtResponse;
import me.stinper.jwtauth.dto.RefreshAccessTokenRequest;
import me.stinper.jwtauth.service.authentication.contract.JwtService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Tag(
        name = "Операции с токенами"
)
@RestController
@RequestMapping("/api/v1/jwt-auth/tokens")
@RequiredArgsConstructor
public class TokenController {
    private final JwtService jwtService;
    private final jakarta.validation.Validator validator;

    @PostMapping("/refresh-access-token")
    @Operation(
            summary = "Получение нового Access-токена",
            description = "Предназначен для получения нового Access-токена (токена доступа к защищенным эндпоинтам) с использованием Refresh-токена",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh-токен",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RefreshAccessTokenRequest.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description =
                                    """
                                    Успешное обновление Access-токена, возвращает новый сгенерированный
                                    Access-токен и старый Refresh-токен (тот, который был использован для его генерации)
                                    """,
                            content = @Content(
                                    schema = @Schema(implementation = JwtResponse.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            }
    )
    public ResponseEntity<JwtResponse> refreshAccessToken(@RequestBody RefreshAccessTokenRequest refreshAccessTokenRequest) {
        Set<ConstraintViolation<RefreshAccessTokenRequest>> constraintViolations = this.validator.validate(refreshAccessTokenRequest);

        if (!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);

        return ResponseEntity.ok(
                jwtService.refreshAccessToken(refreshAccessTokenRequest)
        );
    }

}
