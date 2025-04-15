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

@DisplayName("Unit Tests for PermissionSecurityServiceImpl class")
@ExtendWith(MockitoExtension.class)
class PermissionSecurityServiceImplUnitTest {

    @Spy private AuthorityChecker authorityChecker;

    @InjectMocks
    private PermissionSecurityServiceImpl permissionSecurityService;

    @Test
    void isAllowedToFindAllPermissions_whenUserIsAdmin_thenReturnsTrue() {
        //GIVEN
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(true);

        //WHEN
        boolean hasPermission = permissionSecurityService.isAllowedToFindAllPermissions(user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker, never()).hasAuthority(eq(user), anyString());
    }

    @Test
    void isAllowedToFindAllPermissions_whenUserIsNotAdminButHasPermission_thenReturnsTrue() {
        //GIVEN
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(authorityChecker.hasAuthority(user, "permission.read.find-all-permissions")).thenReturn(true);

        //WHEN
        boolean hasPermission = permissionSecurityService.isAllowedToFindAllPermissions(user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "permission.read.find-all-permissions");
    }

    @Test
    void isAllowedToFindPermissionById_whenUserIsAdmin_thenReturnsTrue() {
        //GIVEN
        final Long permissionId = 1L;
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(true);

        //WHEN
        boolean hasPermission = permissionSecurityService.isAllowedToFindPermissionById(permissionId, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker, never()).hasAuthority(eq(user), anyString());
    }

    @Test
    void isAllowedToFindPermissionById_whenUserIsNotAdminButHasPermission_thenReturnsTrue() {
        //GIVEN
        final Long permissionId = 1L;
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(authorityChecker.hasAuthority(user, "permission.read.find-by-id")).thenReturn(true);

        //WHEN
        boolean hasPermission = permissionSecurityService.isAllowedToFindPermissionById(permissionId, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "permission.read.find-by-id");
    }

    @Test
    void isAllowedToCreatePermission_whenUserIsAdmin_thenReturnsTrue() {
        //GIVEN
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(true);

        //WHEN
        boolean hasPermission = permissionSecurityService.isAllowedToCreatePermission(user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker, never()).hasAuthority(eq(user), anyString());
    }

    @Test
    void isAllowedToCreatePermission_whenUserIsNotAdminButHasPermission_thenReturnsTrue() {
        //GIVEN
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(authorityChecker.hasAuthority(user, "permission.create.create-permission")).thenReturn(true);

        //WHEN
        boolean hasPermission = permissionSecurityService.isAllowedToCreatePermission(user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "permission.create.create-permission");
    }

    @Test
    void isAllowedToUpdatePermissionDescription_whenUserIsAdmin_thenReturnsTrue() {
        //GIVEN
        final Long permissionId = 1L;
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(true);

        //WHEN
        boolean hasPermission = permissionSecurityService.isAllowedToUpdatePermissionDescription(permissionId, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker, never()).hasAuthority(eq(user), anyString());
    }

    @Test
    void isAllowedToUpdatePermissionDescription_whenUserIsNotAdminButHasPermission_thenReturnsTrue() {
        //GIVEN
        final Long permissionId = 1L;
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(authorityChecker.hasAuthority(user, "permission.update.description")).thenReturn(true);

        //WHEN
        boolean hasPermission = permissionSecurityService.isAllowedToUpdatePermissionDescription(permissionId, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "permission.update.description");
    }

    @Test
    void isAllowedToDeletePermission_whenUserIsAdmin_thenReturnsTrue() {
        //GIVEN
        final Long permissionId = 1L;
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(true);

        //WHEN
        boolean hasPermission = permissionSecurityService.isAllowedToDeletePermission(permissionId, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker, never()).hasAuthority(eq(user), anyString());
    }

    @Test
    void isAllowedToDeletePermission_whenUserIsNotAdminButHasPermission_thenReturnsTrue() {
        //GIVEN
        final Long permissionId = 1L;
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(authorityChecker.hasAuthority(user, "permission.delete.delete-by-id")).thenReturn(true);

        //WHEN
        boolean hasPermission = permissionSecurityService.isAllowedToDeletePermission(permissionId, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "permission.delete.delete-by-id");
    }
}