package me.stinper.jwtauth.service.authentication;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.core.security.jwt.JwtProvider;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.dto.JwtResponse;
import me.stinper.jwtauth.dto.RefreshAccessTokenRequest;
import me.stinper.jwtauth.entity.RefreshToken;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.repository.RefreshTokenRepository;
import me.stinper.jwtauth.service.authentication.contract.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final Duration refreshTokenExpiration;

    public JwtServiceImpl(RefreshTokenRepository refreshTokenRepository,
                          JwtProvider jwtProvider,
                          @Value("${app.auth.security.jwt.refresh-token-expiration}") Duration refreshTokenExpiration) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProvider = jwtProvider;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @Override
    public JwtResponse refreshAccessToken(@NonNull RefreshAccessTokenRequest refreshAccessTokenRequest) throws JwtException {
        log.atDebug().log(() -> "[#refreshAccessToken]: Начало выполнение метода. Запрос: " + refreshAccessTokenRequest);

        String refreshToken = refreshAccessTokenRequest.refreshToken();

        jwtProvider.verifyRefreshToken(refreshToken);

        log.atDebug().log("[#refreshAccessToken]: Refresh-токен со значением '{}' успешно верифицирован", refreshToken);

        RefreshToken token = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> {
                    return new JwtException(""); //Сообщение не нужно, оно формируется в обработчике ошибок
                });

        String newAccessToken = jwtProvider.generateAccessToken(token.getUser());

        log.atInfo().log(
                () -> "[#refreshAccessToken]: Для пользователя с эл. почтой '"
                        + token.getUser().getEmail() + "' был сформирован новый Access-токен: " + newAccessToken
        );

        return new JwtResponse(newAccessToken, refreshToken);

    }

    @Override
    public JwtResponse generateTokensPair(@NonNull JwtAuthUserDetails userDetails) {
        log.atDebug().log("[#generateTokensPair]: Начало выполнение метода. Пользователь: {}", userDetails.getUsername());

        String accessToken = jwtProvider.generateAccessToken(userDetails);
        log.atDebug().log("[#generateTokensPair]: Сгенерирован Access-токен: {}", accessToken);

        String refreshToken = jwtProvider.generateRefreshToken(userDetails);
        log.atDebug().log("[#generateTokensPair]: Сгенерирован Refresh-токен: {}", refreshToken);

        RefreshToken token = RefreshToken.builder()
                .user((User) userDetails)
                .token(refreshToken)
                .expiresAt(Instant.now().plus(this.refreshTokenExpiration))
                .build();

        log.atDebug().log(() -> "[#generateTokensPair]: Сформирован объект Refresh-токена: " + token);

        refreshTokenRepository.save(token);

        log.atDebug().log("[#generateTokensPair]: Refresh-токен со значением {} успешно сохранен в БД", refreshToken);

        return new JwtResponse(accessToken, refreshToken);
    }

    @Override
    public void invalidateRefreshTokens(@NonNull JwtAuthUserDetails userDetails) {
        log.atDebug().log("[#invalidateRefreshTokens]: Начало выполнение метода. Пользователь: {}", userDetails.getUsername());

        refreshTokenRepository.deleteByUser_Email(userDetails.getUsername());

        log.atInfo().log("[#invalidateRefreshTokens]: Для пользователя {} были инвалидированы все Refresh-токены", userDetails.getUsername());
    }
}
