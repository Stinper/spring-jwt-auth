package me.stinper.jwtauth.service.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.entity.IdempotencyKey;
import me.stinper.jwtauth.exception.ObjectValueValidationException;
import me.stinper.jwtauth.repository.IdempotencyKeyRepository;
import me.stinper.jwtauth.service.entity.contract.IdempotencyService;
import me.stinper.jwtauth.validation.IdempotencyKeyValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Slf4j
public class IdempotencyServiceImpl implements IdempotencyService {
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;
    private final IdempotencyKeyValidator idempotencyKeyValidator;
    private final Duration idempotencyPeriod;

    public IdempotencyServiceImpl(IdempotencyKeyRepository idempotencyKeyRepository,
                                  ObjectMapper objectMapper,
                                  IdempotencyKeyValidator idempotencyKeyValidator,
                                  @Value("${app.idempotency-period}") Duration idempotencyPeriod) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.objectMapper = objectMapper;
        this.idempotencyKeyValidator = idempotencyKeyValidator;
        this.idempotencyPeriod = idempotencyPeriod;
    }

    @Override
    public <T> T process(@NonNull UUID idempotencyKey, @NonNull Class<T> targetType) throws JsonProcessingException {
        log.atDebug().log("[#process]: Начало выполнение метода. Ключ идемпотентности: '{}'", idempotencyKey);

        IdempotencyKey key = idempotencyKeyRepository.findByKey(idempotencyKey).orElse(null);

        if (key == null || ChronoUnit.MINUTES.between(key.getIssuedAt(), Instant.now()) > idempotencyPeriod.toMinutes()) {
            log.atDebug().log("[#process]: Ключ идемпотентности со значением '{}' не найден или недействителен", idempotencyKey);
            return null;
        }

        String jsonBody = key.getResponseData();
        log.atInfo().log("[#process]: Ключ идемпотентности со значением '{}' найден и действителен. Сохраненные данные взяты из БД", idempotencyKey);

        return objectMapper.readValue(jsonBody, targetType);
    }

    @Override
    public <T> void write(@NonNull UUID idempotencyKey, @NonNull T data) throws JsonProcessingException {
        log.atDebug().log("[#write]: Начало выполнение метода. Ключ идемпотентности: '{}'", idempotencyKey);

        Errors idempotencyKeyErrors = idempotencyKeyValidator.validateObject(idempotencyKey);

        if (idempotencyKeyErrors.hasErrors()) {
            log.atWarn().log(() -> "[#write]: Ошибка валидации ключа идемпотентности: " + idempotencyKeyErrors.getAllErrors());
            throw new ObjectValueValidationException(idempotencyKeyErrors.getAllErrors());
        }

        IdempotencyKey key = IdempotencyKey.builder()
                .key(idempotencyKey)
                .responseData(objectMapper.writeValueAsString(data))
                .build();

        log.atDebug().log(() -> "[#write]: Ключ идемпотентности со значением '" + idempotencyKey + "' успешно построен. Данные, привязанные к ключу: " + data);

        idempotencyKeyRepository.save(key);

        log.atInfo().log("[#write]: Ключ идемпотентности со значением '{}' успешно записан в БД", idempotencyKey);
    }


}
