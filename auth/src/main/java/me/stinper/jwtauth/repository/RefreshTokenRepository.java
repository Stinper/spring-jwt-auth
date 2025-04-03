package me.stinper.jwtauth.repository;

import me.stinper.jwtauth.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @EntityGraph(attributePaths = {"user"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    void deleteByUser_Email(String email);
}
