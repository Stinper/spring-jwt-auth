package me.stinper.jwtauth.service.authentication;

import io.jsonwebtoken.JwtException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.core.security.jwt.service.JwtCreationService;
import me.stinper.jwtauth.core.security.jwt.service.JwtVerificationService;
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
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtVerificationService jwtVerificationService;
    private final JwtCreationService jwtCreationService;

    @Value("${app.auth.security.jwt.refresh-token-expiration}")
    @Setter(AccessLevel.PACKAGE)
    private Duration refreshTokenExpiration;

    @Override
    public JwtResponse refreshAccessToken(@NonNull RefreshAccessTokenRequest refreshAccessTokenRequest) throws JwtException {
        String refreshToken = refreshAccessTokenRequest.refreshToken();

        log.atDebug().log("[#refreshAccessToken]: Начало выполнение метода \n\tRefresh-токен: '{}'", refreshToken);

        jwtVerificationService.verifyTokenSignature(refreshToken);

        log.atDebug().log("[#refreshAccessToken]: Refresh-токен успешно верифицирован \n\tЗначение токена: '{}'", refreshToken);

        RefreshToken token = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> {
                    log.atDebug().log("[#refreshAccessToken]: Успешно верифицированный Refresh-токен не был найден в БД " +
                            "\n\tЗначение токена: '{}'", refreshToken
                    );

                    return new JwtException(""); //Сообщение не нужно, оно формируется в обработчике ошибок
                });

        String newAccessToken = jwtCreationService.createAccessToken(token.getUser());

        log.atInfo().log(
                () -> "[#refreshAccessToken]: Для пользователя с эл. почтой '"
                        + token.getUser().getEmail() + "' был сформирован новый Access-токен " +
                        "\n\tЗначение токена: '" + newAccessToken + "'"
        );

        return new JwtResponse(newAccessToken, refreshToken);

    }

    @Override
    public JwtResponse generateTokensPair(@NonNull JwtAuthUserDetails userDetails) {
        log.atDebug().log("[#generateTokensPair]: Начало выполнение метода. UUID: '{}'", userDetails.getUuid());

        String accessToken = jwtCreationService.createAccessToken(userDetails);
        log.atDebug().log("[#generateTokensPair]: Для пользователя '{}' был сгенерирован Access-токен \n\tЗначение токена: '{}'",
                userDetails.getUuid(), accessToken
        );

        String refreshToken = jwtCreationService.createRefreshToken(userDetails);
        log.atDebug().log("[#generateTokensPair]: Для пользователя '{}' был сгенерирован Refresh-токен \n\tЗначение токена: '{}'",
                userDetails.getUuid(), refreshToken
        );

        RefreshToken token = RefreshToken.builder()
                .user((User) userDetails)
                .token(refreshToken)
                .expiresAt(Instant.now().plus(this.refreshTokenExpiration))
                .build();

        log.atDebug().log(() -> "[#generateTokensPair]: Сформирован объект Refresh-токена: \n\tПользователь: '" + token.getUser().getUuid() +
                "'\n\tЗначение токена: '" + refreshToken +
                "'\n\tТокен действителен до: '" + token.getExpiresAt() + "'"
        );

        refreshTokenRepository.save(token);

        log.atDebug().log("[#generateTokensPair]: Refresh-токен успешно сохранен в БД \n\tЗначение токена: '{}'", refreshToken);

        return new JwtResponse(accessToken, refreshToken);
    }

    @Override
    public void invalidateRefreshTokens(@NonNull JwtAuthUserDetails userDetails) {
        log.atDebug().log("[#invalidateRefreshTokens]: Начало выполнение метода. UUID: {}", userDetails.getUuid());

        refreshTokenRepository.deleteByUser_Email(userDetails.getUsername());

        log.atInfo().log("[#invalidateRefreshTokens]: Для пользователя '{}' были инвалидированы все Refresh-токены",
                userDetails.getUuid()
        );
    }
}
