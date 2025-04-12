package me.stinper.jwtauth.service.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.entity.IdempotencyKey;
import me.stinper.jwtauth.exception.IdempotencyKeyExpiredException;
import me.stinper.jwtauth.repository.IdempotencyKeyRepository;
import me.stinper.jwtauth.service.entity.contract.IdempotencyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.idempotency-period}")
    @Setter(AccessLevel.PACKAGE)
    private Duration idempotencyPeriod;

    @Override
    @Transactional
    public <T> T process(@NonNull UUID idempotencyKey, @NonNull Supplier<T> serviceOperation, @NonNull Class<T> targetType)
            throws JsonProcessingException {
        log.atDebug().log("[#process]: Начало выполнение метода. Ключ идемпотентности: '{}'", idempotencyKey);

        IdempotencyKey key = idempotencyKeyRepository.findByKey(idempotencyKey).orElse(null);

        if (key != null) {
            if (ChronoUnit.MINUTES.between(key.getIssuedAt(), Instant.now()) <= idempotencyPeriod.toMinutes()) {
                log.atInfo().log("[#process]: Ключ идемпотентности со значением '{}' найден и действителен. " +
                        "Данные взяты из БД, операция не выполнена повторно", idempotencyKey);

                return objectMapper.readValue(key.getResponseData(), targetType);
            }

            log.atWarn().log("[#process]: Ключ идемпотентности со значением '{}' имеет истекший срок действия", idempotencyKey);
            throw new IdempotencyKeyExpiredException("messages.idempotency-key.expired", idempotencyKey);
        }

        log.atDebug().log("[#process]: Ключ идемпотентности со значением '{}' не существует, подготовка к выполнению операции",
                idempotencyKey
        );

        T responseData = serviceOperation.get();

        log.atDebug().log("[#process]: Операция успешно выполнена, подготовка к сохранению результата операции");

        IdempotencyKey newIdempotencyKey = IdempotencyKey.builder()
                .key(idempotencyKey)
                .responseData(objectMapper.writeValueAsString(responseData))
                .build();

        log.atDebug().log(() -> "[#process]: Ключ идемпотентности со значением '" + idempotencyKey + "' успешно построен " +
                "\n\tДанные, привязанные к ключу: " + responseData
        );

        idempotencyKeyRepository.save(newIdempotencyKey);

        log.atInfo().log("[#process]: Ключ идемпотентности со значением '{}' успешно записан в БД", idempotencyKey);

        return responseData;
    }
}
