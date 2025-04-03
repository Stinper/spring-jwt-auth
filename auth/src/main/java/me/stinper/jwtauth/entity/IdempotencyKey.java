package me.stinper.jwtauth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key", nullable = false, unique = true)
    private UUID key;

    @Column(name = "issued_at", nullable = false, insertable = false, updatable = false)
    private Instant issuedAt = Instant.now();

    @Column(name = "response_data", nullable = false, columnDefinition = "TEXT")
    private String responseData;

}
