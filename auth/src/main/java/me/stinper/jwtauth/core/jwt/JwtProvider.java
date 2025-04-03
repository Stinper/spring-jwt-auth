package me.stinper.jwtauth.core.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import me.stinper.jwtauth.core.security.JwtAuthUserDetails;
import org.springframework.lang.NonNull;

import java.util.UUID;

/**
 * Интерфейс, содержащий методы для генерации и верификации access/refresh токенов
 */
public interface JwtProvider {

    /**
     * Генерирует новый Access Token, на основе переданного объекта UserDetails
     * @param userDetails ключевая информация о пользователе
     * @return сгенерированный Access Token
     */
    String generateAccessToken(@NonNull JwtAuthUserDetails userDetails);

    /**
     * Генерирует новый Refresh Token, на основе переданного объекта UserDetails
     * @param userDetails ключевая информация о пользователе
     * @return сгенерированный Refresh Token
     */
    String generateRefreshToken(@NonNull JwtAuthUserDetails userDetails);


    /**
     * Проводит верификацию подписи Access-токена с помощью публичного ключа
     * @param token токен, подпись которого необходимо верифицировать
     * @throws JwtException верификация подписи токена не удалась
     */
    void verifyAccessToken(@NonNull String token) throws JwtException;

    /**
     * Проводит верификацию подписи Refresh-токена с помощью публичного ключа
     * @param token токен, подпись которого необходимо верифицировать
     * @throws JwtException верификация подписи токена не удалась
     */
    void verifyRefreshToken(@NonNull String token) throws JwtException;

    /**
     * Извлекает Claims из заданного access токена
     * @param accessToken токен
     * @return объект Claims
     */
    Claims accessTokenClaims(@NonNull String accessToken);

    /**
     * Извлекает Claims из заданного refresh токена
     * @param refreshToken токен
     * @return объект Claims
     */
    Claims refreshTokenClaims(@NonNull String refreshToken);

    /**
     * Извлекает заголовок из заданного access токена
     * @param accessToken токен
     * @return Объект заголовка
     */
    Header accessTokenHeader(@NonNull String accessToken);

    /**
     * Извлекает заголовок из заданного refresh токена
     * @param refreshToken токен
     * @return Объект заголовка
     */
    Header refreshTokenHeader(@NonNull String refreshToken);
}
