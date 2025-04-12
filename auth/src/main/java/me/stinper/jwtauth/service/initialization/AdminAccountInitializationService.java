package me.stinper.jwtauth.service.initialization;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.repository.RoleRepository;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.service.initialization.contract.InitializationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminAccountInitializationService implements InitializationService<User> {
    private final InitializationService<Role> roleInitializationService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.auth.security.admin-email}")
    @Setter(AccessLevel.PACKAGE)
    private String adminAccountEmail;

    @Value("${app.auth.security.admin-password}")
    @Setter(AccessLevel.PACKAGE)
    private String adminAccountPassword;

    @Value("${app.auth.security.admin-role-name}")
    @Setter(AccessLevel.PACKAGE)
    private String adminRoleName;

    @Override
    @Transactional
    public User initialize() {
        if (this.isAlreadyInitialized()) {
            log.atInfo().log("[#initialize]: Инициализация учетной записи администратора не требуется");
            return null;
        }

        User.UserBuilder adminAccountBuilder = User.builder()
                .email(this.adminAccountEmail)
                .password(passwordEncoder.encode(this.adminAccountPassword));

        Optional<Role> adminRole = Optional.ofNullable(roleInitializationService.initialize());

        if (adminRole.isEmpty())
            adminRole = roleRepository.findByRoleNameIgnoreCase(this.adminRoleName);

        adminRole.ifPresentOrElse(
                (role) -> adminAccountBuilder.roles(List.of(role)),
                () -> adminAccountBuilder.roles(Collections.emptyList())
        );

        User adminAccount = adminAccountBuilder.build();
        adminAccount.setIsEmailVerified(true);

        adminAccount = userRepository.save(adminAccount);

        log.atInfo().log("[#initialize]: Инициализация учетной записи администратора прошла успешно \n\tUUID: '{}' \n\tEmail: '{}'",
                adminAccount.getUuid(),
                adminAccount.getEmail()
        );

        return adminAccount;
    }

    @Override
    public boolean isAlreadyInitialized() {
        log.atInfo().log("[#isAlreadyInitialized]: Стратегия инициализации учетной записи администратора: '{}'",
                this.getInitializationMode().toString()
        );

        if (this.getInitializationMode() == InitializationMode.ON_TABLE_EMPTY)
            return !userRepository.isTableEmpty();

        return userRepository.existsByEmailIgnoreCase(this.adminAccountEmail);
    }
}
