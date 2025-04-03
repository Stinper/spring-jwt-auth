package me.stinper.jwtauth.service.initialization;

import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.repository.RoleRepository;
import me.stinper.jwtauth.service.initialization.contract.InitializationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RoleInitializationService implements InitializationService<Role> {
    private final RoleRepository roleRepository;
    private final String adminRoleName;

    public RoleInitializationService(RoleRepository roleRepository,
                                     @Value("${app.auth.security.admin-role-name}") String adminRoleName) {
        this.roleRepository = roleRepository;
        this.adminRoleName = adminRoleName;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Role initialize() {
        if (this.isAlreadyInitialized()) {
            log.atInfo().log("Инициализация роли администратора не требуется");
            return null;
        }

        Role role = Role.builder()
                .roleName(this.adminRoleName)
                .build();

        role = roleRepository.save(role);
        log.atInfo().log("Инициализация роли администратора прошла успешно. Имя роли - {}", this.adminRoleName);

        return role;
    }

    @Override
    public boolean isAlreadyInitialized() {
        log.atInfo().log("Стратегия инициализации роли администратора - {}", this.getInitializationMode().toString());

        if (this.getInitializationMode() == InitializationMode.ON_TABLE_EMPTY)
            return !roleRepository.isTableEmpty();

        return roleRepository.existsByRoleNameIgnoreCase(this.adminRoleName);
    }
}
