package me.stinper.jwtauth.repository;

import me.stinper.jwtauth.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleNameIgnoreCase(String roleName);

    @Override
    Page<Role> findAll(Pageable pageable);

    void deleteByRoleNameIgnoreCase(String roleName);

    boolean existsByRoleNameIgnoreCase(String roleName);

    @Query("select case when count(r.id) > 0 then false else true end from Role r")
    boolean isTableEmpty();
}
