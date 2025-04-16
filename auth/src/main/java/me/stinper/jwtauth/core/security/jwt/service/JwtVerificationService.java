package me.stinper.jwtauth.core.security.jwt.service;

import io.jsonwebtoken.JwtException;
import org.springframework.lang.NonNull;

public interface JwtVerificationService {

    /**
     * Верифицирует подпись предоставленного токена. Если метод не выбросил исключение - верификация
     * прошла успешно
     * @param token токен
     * @throws JwtException не удалось верифицировать подпись токена
     */
    void verifyTokenSignature(@NonNull String token) throws JwtException;

}
