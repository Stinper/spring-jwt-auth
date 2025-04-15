package me.stinper.jwtauth.repository;

import me.stinper.jwtauth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Set<Permission> findAllByPermissionIn(Set<String> permissions);

    boolean existsByPermissionIgnoreCase(String permission);

    @Query("select case when count(p.id) > 0 then false else true end from Permission p")
    boolean isTableEmpty();
}
