package me.stinper.jwtauth.service.security;

import me.stinper.jwtauth.core.security.AuthorityChecker;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for RoleSecurityServiceImpl class")
@ExtendWith(MockitoExtension.class)
class RoleSecurityServiceImplUnitTest {
    @Spy private AuthorityChecker authorityChecker;

    @InjectMocks
    private RoleSecurityServiceImpl roleSecurityService;

    @Test
    void isAllowedToFindAllRoles_whenUserIsAdmin_thenReturnsTrue() {
        //GIVEN
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(true);

        //WHEN
        boolean hasPermission = roleSecurityService.isAllowedToFindAllRoles(user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker, never()).hasAuthority(eq(user), anyString());
    }

    @Test
    void isAllowedToFindAllRoles_whenUserIsNotAdminButHasPermission_thenReturnsTrue() {
        //GIVEN
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(authorityChecker.hasAuthority(user, "role.read.find-all-roles")).thenReturn(true);

        //WHEN
        boolean hasPermission = roleSecurityService.isAllowedToFindAllRoles(user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "role.read.find-all-roles");
    }

    @Test
    void isAllowedToFindRoleByName_whenUserIsAdmin_thenReturnsTrue() {
        //GIVEN
        final String roleName = "ROLE_USER";
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(true);

        //WHEN
        boolean hasPermission = roleSecurityService.isAllowedToFindRoleByName(roleName, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker, never()).hasAuthority(eq(user), anyString());
    }

    @Test
    void isAllowedToFindRoleByName_whenUserIsNotAdminButHasPermission_thenReturnsTrue() {
        //GIVEN
        final String roleName = "ROLE_USER";
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(authorityChecker.hasAuthority(user, "role.read.find-role-by-name")).thenReturn(true);

        //WHEN
        boolean hasPermission = roleSecurityService.isAllowedToFindRoleByName(roleName, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "role.read.find-role-by-name");
    }

    @Test
    void isAllowedToCreateRole_whenUserIsAdmin_thenReturnsTrue() {
        //GIVEN
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(true);

        //WHEN
        boolean hasPermission = roleSecurityService.isAllowedToCreateRole(user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker, never()).hasAuthority(eq(user), anyString());
    }

    @Test
    void isAllowedToCreateRole_whenUserIsNotAdminButHasPermission_thenReturnsTrue() {
        //GIVEN
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(authorityChecker.hasAuthority(user, "role.create.create-role")).thenReturn(true);

        //WHEN
        boolean hasPermission = roleSecurityService.isAllowedToCreateRole(user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "role.create.create-role");
    }

    @Test
    void isAllowedToUpdateRolePermissions_whenUserIsAdmin_thenReturnsTrue() {
        //GIVEN
        final String roleName = "ROLE_USER";
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(true);

        //WHEN
        boolean hasPermission = roleSecurityService.isAllowedToUpdateRolePermissions(roleName, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker, never()).hasAuthority(eq(user), anyString());
    }

    @Test
    void isAllowedToUpdateRolePermissions_whenUserIsNotAdminButHasPermission_thenReturnsTrue() {
        //GIVEN
        final String roleName = "ROLE_USER";
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(authorityChecker.hasAuthority(user, "role.update.partial.permissions-list")).thenReturn(true);

        //WHEN
        boolean hasPermission = roleSecurityService.isAllowedToUpdateRolePermissions(roleName, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "role.update.partial.permissions-list");
    }

    @Test
    void isAllowedToDeleteRoleByName_whenUserIsAdmin_thenReturnsTrue() {
        //GIVEN
        final String roleName = "ROLE_USER";
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(true);

        //WHEN
        boolean hasPermission = roleSecurityService.isAllowedToDeleteRoleByName(roleName, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker, never()).hasAuthority(eq(user), anyString());
    }

    @Test
    void isAllowedToDeleteRoleByName_whenUserIsNotAdminButHasPermission_thenReturnsTrue() {
        //GIVEN
        final String roleName = "ROLE_USER";
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(authorityChecker.hasAuthority(user, "role.delete.delete-role-by-name")).thenReturn(true);

        //WHEN
        boolean hasPermission = roleSecurityService.isAllowedToDeleteRoleByName(roleName, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "role.delete.delete-role-by-name");
    }
}