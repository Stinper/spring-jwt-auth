package me.stinper.jwtauth.core.security.permission;

import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.JwtAuthApplication;
import me.stinper.jwtauth.core.security.permission.annotation.OperationPermission;
import me.stinper.jwtauth.core.security.permission.annotation.Permissions;
import me.stinper.jwtauth.entity.Permission;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class PermissionScannerImpl extends AbstractPermissionScanner {

    public PermissionScannerImpl(ObjectProvider<JwtAuthApplication> objectProvider) {
        super(objectProvider);
    }

    @Override
    public Set<Permission> scanPermissionsFromPackage(@NonNull String packageName) throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);

        Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(packageName);
        log.atDebug().log("[#scanPermissionsFromPackage]: Найдено {} классов в целевом пакете {}", beanDefinitions.size(), packageName);

        Set<Permission> result = new HashSet<>();

        for (BeanDefinition beanDefinition : beanDefinitions) {
            String beanClassName = beanDefinition.getBeanClassName();
            Class<?> beanClass = Class.forName(beanClassName);

            Method[] methods = beanClass.getDeclaredMethods();

            for (Method method : methods) {
                if (method.isAnnotationPresent(OperationPermission.class))
                    result.add(this.handleOperationPermissionAnnotation(method.getAnnotation(OperationPermission.class)));

                if (method.isAnnotationPresent(Permissions.class))
                    result.addAll(this.handlePermissionsAnnotation(method.getAnnotation(Permissions.class)));
            }
        }

        log.atDebug().log("[#scanPermissionsFromPackage]: Найдено {} прав в целевом пакете {}", result.size(), packageName);

        return result;
    }
}
