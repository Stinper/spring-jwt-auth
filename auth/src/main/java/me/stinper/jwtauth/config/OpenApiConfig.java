package me.stinper.jwtauth.config;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Документация API к серверу авторизации")
                                .version("0.0.1")
                )
                .components(
                        new Components()
                                .responses(apiResponses())
                                .parameters(apiParameters())
                );
    }

    private Map<String, ApiResponse> apiResponses() {
        Map<String, ApiResponse> responses = new HashMap<>();

        responses.put("PagedOK", new ApiResponse()
                .description("Успешное выполнение операции")
                .content(new Content()
                        .addMediaType(
                                MediaType.APPLICATION_JSON_VALUE,
                                new io.swagger.v3.oas.models.media.MediaType()
                                        .schema(new Schema<>()
                                                .$ref("#/components/schemas/PagedModel")
                                        )
                        )
                )
        );


        responses.put("Created", new ApiResponse()
                .description("Ресурс успешно создан")
                .content(
                        new Content()
                                .addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType())
                )
                .headers(
                        Map.of(
                                "Location",
                                new Header()
                                        .description("Расположение (URI) успешно созданного ресурса")
                                        .example(".../api/v1/jwt-auth/users/01960a1c-957a-7651-8e36-09a22473fecd")
                        )
                )
        );

        responses.put("NoContent", new ApiResponse()
                .description("Успешное выполнение операции. Тело ответа не требуется")
                .content(
                        new Content()
                                .addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType())
                )
        );

        responses.put("Conflict", new ApiResponse()
                .description("Конфликт при выполнении операции")
                .content(
                        new Content()
                                .addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType())
                )
        );

        responses.put("NotFound", new ApiResponse()
                .description("Ресурс не найден")
                .content(
                        new Content()
                                .addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType())
                )
        );


        return responses;
    }

    public Map<String, Parameter> apiParameters() {
        Map<String, Parameter> parameters = new HashMap<>();

        parameters.put("Authorization", new Parameter()
                .name("Authorization")
                .description("JWT-токен для аутентификации")
                .required(true)
                .in(ParameterIn.HEADER.toString())
                .schema(new Schema<String>().type("string").format("Bearer token"))
                .example("Bearer ...")
        );

        return parameters;
    }
}
