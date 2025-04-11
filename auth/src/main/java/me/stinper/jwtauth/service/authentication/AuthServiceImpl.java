package me.stinper.jwtauth.service.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.dto.JwtResponse;
import me.stinper.jwtauth.dto.user.LoginRequest;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.exception.ResourceNotFoundException;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.service.authentication.contract.AuthService;
import me.stinper.jwtauth.service.authentication.contract.JwtService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public JwtResponse login(@NonNull LoginRequest loginRequest) throws AuthenticationException {
        try {
            log.atDebug().log("[#login]: Попытка аутентификации пользователя с эл. почтой '{}'", loginRequest.email());

            UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken
                    .unauthenticated(
                            loginRequest.email(),
                            loginRequest.password()
                    );

            authenticationManager.authenticate(authenticationToken);

            User user = userRepository
                    .findByEmailIgnoreCase(loginRequest.email())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("messages.user.not-found.email", loginRequest.email())
                    );

            log.atDebug().log("[#login]: Аутентификация пользователя с эл. почтой '{}' прошла успешно", loginRequest.email());

            return jwtService.generateTokensPair(user);
        }
        catch (BadCredentialsException bce) {
            log.atDebug().log("[#login]: Аутентификация пользователя с эл. почтой '{}' не удалась. Неверные учетные данные", loginRequest.email());
            throw new BadCredentialsException(bce.getMessage(), bce);
        }
        catch (LockedException le) {
            log.atDebug().log("[#login]: Аутентификация пользователя с эл. почтой '{}' не удалась. Аккаунт был заблокирован", loginRequest.email());
            throw new LockedException(le.getMessage(), le);
        }
        catch (DisabledException de) {
            log.atDebug().log("[#login]: Аутентификация пользователя с эл. почтой '{}' не удалась. Аккаунт был деактивирован", loginRequest.email());
            throw new DisabledException(de.getMessage(), de);
        }
    }

    @Override
    public void logout(@NonNull JwtAuthUserDetails userDetails) {
        log.atDebug().log("[#logout]: Начало выполнения метода. Пользователь: {}", userDetails.getUsername());

        jwtService.invalidateRefreshTokens(userDetails);
    }
}
