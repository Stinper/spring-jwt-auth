package me.stinper.jwtauth.service.authentication.contract;

import me.stinper.jwtauth.core.security.JwtAuthUserDetails;
import me.stinper.jwtauth.dto.JwtResponse;
import me.stinper.jwtauth.dto.user.LoginRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Сервис, отвечающий за аутентификацию пользователей и выдачу им Access & Refresh токенов
 */
public interface AuthService {
    /**
     * Метод, который проводит аутентификацию пользователя, и возвращает пару
     * access & refresh токенов в случае успеха
     * @param loginRequest Учетные данные пользователя
     * @return Если учетные данные верны - возвращает пару access & refresh токенов
     * @throws AuthenticationException Если учетные данные пользователя некорректны
     */
    JwtResponse login(@NonNull LoginRequest loginRequest) throws AuthenticationException;

    /**
     * Метод, который инвалидирует refresh токен, связанный с пользователем
     * @param userDetails Объект UserDetails, связанный с текущим пользователем (тем, который отправляет запрос)
     */
    void logout(@NonNull JwtAuthUserDetails userDetails);

}
