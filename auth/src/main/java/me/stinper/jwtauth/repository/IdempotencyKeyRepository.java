package me.stinper.jwtauth.repository;

import me.stinper.jwtauth.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByKey(UUID key);

    boolean existsByKey(UUID key);

    @Modifying
    @Transactional
    int deleteByIssuedAtBefore(Instant period);
}
