package me.stinper.jwtauth.service.initialization;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.core.security.permission.PermissionScanner;
import me.stinper.jwtauth.entity.Permission;
import me.stinper.jwtauth.repository.PermissionRepository;
import me.stinper.jwtauth.service.initialization.contract.InitializationService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionInitializationService implements InitializationService<List<Permission>> {
    private final PermissionRepository permissionRepository;
    private final PermissionScanner permissionScanner;
    private Set<Permission> scannedPermissions;
    private List<Permission> existingPermissions;

    @PostConstruct
    void init() throws Exception {
        Set<Permission> scannedPermissions = new HashSet<>();
        List<String> scanCandidatePackages = permissionScanner.resolveCandidatePackages();

        for (String pack : scanCandidatePackages)
            scannedPermissions.addAll(permissionScanner.scanPermissionsFromPackage(pack));

        this.scannedPermissions = scannedPermissions;
        this.existingPermissions = permissionRepository.findAll();
    }

    @Override
    public List<Permission> initialize() {
        if (this.isAlreadyInitialized()) {
            log.atInfo().log("Инициализация прав доступа не требуется");
            return null;
        }

        Set<Permission> uninitializedPermissions = findPermissionDifference(scannedPermissions, existingPermissions);

        log.atDebug().log(
                () -> "Найдено " + uninitializedPermissions.size() + " прав доступа, которые не были инициализированы:\n" +
                        uninitializedPermissions.stream()
                                .map(p -> "\t[" + p.getPermission() + ", " + p.getDescription() + "]")
                                .collect(Collectors.joining("\n"))
        );

        var initializedPermissions = permissionRepository.saveAll(uninitializedPermissions);

        log.atInfo().log("Все права доступа успешно инициализированы");

        return initializedPermissions;
    }

    @Override
    public boolean isAlreadyInitialized() {
        log.atInfo().log("Стратегия инициализации прав доступа - {}", this.getInitializationMode().toString());

        if (this.getInitializationMode() == InitializationMode.ON_TABLE_EMPTY)
            return !permissionRepository.isTableEmpty();

        return findPermissionDifference(scannedPermissions, existingPermissions).isEmpty();
    }

    @Override
    public InitializationMode getInitializationMode() {
        return InitializationMode.ON_RELOAD;
    }


    private static Set<Permission> findPermissionDifference(@NonNull Collection<Permission> scannedPermissions,
                                                            @NonNull Collection<Permission> existingPermissions) {
        return scannedPermissions.stream()
                .filter(scannerPerm ->
                        existingPermissions.stream()
                                .noneMatch(existingPerm -> scannerPerm.getPermission().equals(existingPerm.getPermission()))
                )
                .collect(Collectors.toSet());
    }
}
