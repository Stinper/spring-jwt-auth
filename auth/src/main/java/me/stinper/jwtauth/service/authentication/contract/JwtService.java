package me.stinper.jwtauth.service.authentication.contract;

import io.jsonwebtoken.JwtException;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.dto.JwtResponse;
import me.stinper.jwtauth.dto.RefreshAccessTokenRequest;
import org.springframework.lang.NonNull;

/**
 * Сервис, служащий для генерации Access & Refresh токенов
 */
public interface JwtService {

    /**
     * Метод, служащий для получения нового access токена по ранее выданному refresh токену
     * @param refreshAccessTokenRequest Объект запроса
     * @return Возвращает объект, в котором будет содержаться новый access токен
     * @throws JwtException если в процессе работы с JWT произошла ошибка
     */
    JwtResponse refreshAccessToken(@NonNull RefreshAccessTokenRequest refreshAccessTokenRequest) throws JwtException;

    /**
     * Метод, служащий для генерации пары токенов (Access & Refresh)
     * @param userDetails Объект, содержащий информацию о пользователе, для которого будут сгенерированы токены
     * @return Объект, содержащий пару токенов
     */
    JwtResponse generateTokensPair(@NonNull JwtAuthUserDetails userDetails);

    /**
     * Удаляет все Refresh токены, связанные с конкретным пользователем
     * @param userDetails объект, содержащий информацию о пользователе
     */
    void invalidateRefreshTokens(@NonNull JwtAuthUserDetails userDetails);

}
