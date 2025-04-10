package me.stinper.jwtauth.core.security.permission;

import me.stinper.jwtauth.entity.Permission;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

public interface PermissionScanner {
    List<String> resolveCandidatePackages();

    Set<Permission> scanPermissionsFromPackage(@NonNull String packageName) throws Exception;
}
