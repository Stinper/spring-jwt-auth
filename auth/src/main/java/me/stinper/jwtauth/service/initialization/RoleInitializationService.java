package me.stinper.jwtauth.service.initialization;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
@RequiredArgsConstructor
public class RoleInitializationService implements InitializationService<Role> {
    private final RoleRepository roleRepository;

    @Value("${app.auth.security.admin-role-name}")
    @Setter(AccessLevel.PACKAGE)
    private String adminRoleName;

    @Value("${app.auth.security.admin-role-prefix}")
    @Setter(AccessLevel.PACKAGE)
    private String adminRolePrefix;

    @Value("${app.auth.security.initialization.admin-role-init-mode}")
    @Setter(AccessLevel.PACKAGE)
    private InitializationMode roleInitializationMode;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Role initialize() {
        if (this.isAlreadyInitialized()) {
            log.atInfo().log("[#initialize]: Инициализация роли администратора не требуется");
            return null;
        }

        Role role = Role.builder()
                .roleName(this.adminRoleName)
                .prefix(this.adminRolePrefix)
                .build();

        role = roleRepository.save(role);
        log.atInfo().log("[#initialize]: Инициализация роли администратора прошла успешно \n\tИмя роли: '{}'", this.adminRoleName);

        return role;
    }

    @Override
    public boolean isAlreadyInitialized() {
        log.atInfo().log("[#isAlreadyInitialized]: Стратегия инициализации роли администратора: '{}'",
                this.getInitializationMode().toString()
        );

        if (this.getInitializationMode() == InitializationMode.ON_TABLE_EMPTY)
            return !roleRepository.isTableEmpty();

        return roleRepository.existsByRoleNameIgnoreCase(this.adminRoleName);
    }

    @Override
    public InitializationMode getInitializationMode() {
        return this.roleInitializationMode;
    }
}
