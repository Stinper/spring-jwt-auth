package me.stinper.jwtauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.Headers;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.dto.JwtResponse;
import me.stinper.jwtauth.dto.user.LoginRequest;
import me.stinper.jwtauth.service.authentication.contract.AuthService;
import me.stinper.jwtauth.service.entity.contract.IdempotencyService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@Tag(
        name = "Операции аутентификации"
)
@RestController
@RequestMapping("/api/v1/jwt-auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final IdempotencyService idempotencyService;
    private final jakarta.validation.Validator validator;

    @PostMapping("/login")
    @Operation(
            summary = "Вход",
            description = "Предназначен для получения пары (Access/Refresh) JWT токенов",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Учетные данные для входа",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный вход, возвращает пару JWT токенов",
                            content = @Content(
                                    schema = @Schema(implementation = JwtResponse.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
            },
            parameters = {
                    @Parameter(
                            name = Headers.X_IDEMPOTENCY_KEY,
                            description = "Уникальный ключ идемпотентности в формате UUID",
                            required = true,
                            example = "019555f7-af82-70d3-914d-271fbcb87b40"
                    )
            }
    )
    public ResponseEntity<JwtResponse> login(@RequestHeader(Headers.X_IDEMPOTENCY_KEY) UUID idempotencyKey,
                                             @RequestBody LoginRequest loginRequest)
            throws AuthenticationException {
        Set<ConstraintViolation<LoginRequest>> constraintViolations = this.validator.validate(loginRequest);

        if (!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);

        return ResponseEntity
                .ok(
                        idempotencyService.wrap(idempotencyKey, () -> authService.login(loginRequest), JwtResponse.class)
                );
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Выход",
            description =
                    """
                    Предназначен для инвалидации ранее выданного Refresh-токена.
                    После выполнения этой операции этот токен больше нельзя будет использовать для получения новых Access-токенов
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Токен успешно инвалидирован",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
            },
            parameters = {
                    @Parameter(ref = "Authorization")
            }
    )
    public ResponseEntity<?> logout(@AuthenticationPrincipal JwtAuthUserDetails userDetails) {
        authService.logout(userDetails);
        return ResponseEntity.ok().build();
    }

}
