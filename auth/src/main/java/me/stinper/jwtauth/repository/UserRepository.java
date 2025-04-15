package me.stinper.jwtauth.repository;

import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("SELECT u FROM User u JOIN FETCH u.roles roles JOIN FETCH roles.permissions WHERE upper(u.email) = upper(:email)")
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select case when count(u.uuid) > 0 then false else true end from User u")
    boolean isTableEmpty();

    boolean existsByRoles(Set<Role> roles);
}
