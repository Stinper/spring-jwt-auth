package me.stinper.jwtauth.core.scheduling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.repository.IdempotencyKeyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyKeysCleanScheduler {
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    @Value("${app.idempotency-period}")
    private Duration idempotencyKeyExpiration;

    @Scheduled(cron = "@hourly")
    @Async
    public void cleanExpiredIdempotencyKeys() {
        Instant cleanPeriod = Instant.now().minus(this.idempotencyKeyExpiration);
        int recordsCleaned = idempotencyKeyRepository.deleteByIssuedAtBefore(cleanPeriod);

        if (recordsCleaned > 0)
            log.info("[#cleanExpiredIdempotencyKeys]: Проведена очистка истекших ключей идемпотентности. Затронуто записей: {}", recordsCleaned);
        else
            log.info("[#cleanExpiredIdempotencyKeys]: Очистка истекших ключей идемпотентности была запущена, но не затронула ни одной записи");
    }
}
