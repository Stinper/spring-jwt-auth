package me.stinper.jwtauth.core.swagger;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Parameters({
        @Parameter(
                name = "Authorization",
                description = "JWT-токен для аутентификации",
                required = true,
                schema = @Schema(type = "string", format = "Bearer token"),
                example = "Bearer ...",
                in = ParameterIn.HEADER
        )
})
public @interface AuthorizationHeaderDescription {
}
