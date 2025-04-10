package me.stinper.jwtauth.core.security.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.JwtAuthApplication;
import me.stinper.jwtauth.core.security.permission.annotation.PermissionScan;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPermissionScanner implements PermissionScanner {
    private final ObjectProvider<JwtAuthApplication> objectProvider;

    @Override
    public List<String> resolveCandidatePackages() {
        Class<?> mainClass = objectProvider.getIfAvailable().getClass();
        PermissionScan permissionScan = mainClass.getAnnotation(PermissionScan.class);

        if (permissionScan == null) {
            throw new IllegalStateException("Аннотация @PermissionScan над главным классом (@SpringBootApplication) не найдена." +
                    " Предоставьте эту аннотацию для возможности автоматического сканирования и поиска прав доступа");
        }

        log.atDebug().log(() -> "[#resolveCandidatePackages]: Пакеты для сканирования прав доступа успешно найдены: "
                + Arrays.toString(permissionScan.packages()));

        return Arrays.asList(permissionScan.packages());
    }
}
