package me.stinper.jwtauth.repository;

import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Page<User> findAllByDeactivatedAtIsNull(Pageable pageable);

    @EntityGraph(attributePaths = {"roles"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select case when count(u.uuid) > 0 then false else true end from User u")
    boolean isTableEmpty();

    boolean existsByRoles(List<Role> roles);
}
