package me.stinper.jwtauth.core.security;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Getter
@Setter(AccessLevel.PACKAGE)
public class ApplicationPropertiesAdminRoleNameHolder implements AdminRoleNameHolder {

    @Value("${app.auth.security.admin-role-name}")
    String adminRoleName;

    @Override
    public String adminRoleName() {
        return this.adminRoleName;
    }
}
