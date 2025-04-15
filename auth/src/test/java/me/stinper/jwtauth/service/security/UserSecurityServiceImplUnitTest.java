package me.stinper.jwtauth.service.security;

import me.stinper.jwtauth.core.security.AuthorityChecker;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.service.entity.support.ActiveUserFilterStrategy;
import me.stinper.jwtauth.service.entity.support.AllUserFilterStrategy;
import me.stinper.jwtauth.service.entity.support.UserFilterStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for UserSecurityServiceImpl class")
@ExtendWith(MockitoExtension.class)
class UserSecurityServiceImplUnitTest {
    private static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";

    @Spy private AuthorityChecker authorityChecker;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private UserSecurityServiceImpl userSecurityService;

    @Test
    void isAllowedToFindAllUsers_whenUserIsAdmin_thenReturnsTrue() {
        //GIVEN
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(true);

        //WHEN
        boolean hasPermission = userSecurityService.isAllowedToFindAllUsers(user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker, never()).hasAuthority(eq(user), anyString());

        verifyNoInteractions(userRepository);
    }


    @Test
    void isAllowedToFindAllUsers_whenUserIsNotAdminButHasPermission_thenReturnsTrue() {
        //GIVEN
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(authorityChecker.hasAuthority(user, "user.read.find-all-users")).thenReturn(true);

        //WHEN
        boolean hasPermission = userSecurityService.isAllowedToFindAllUsers(user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "user.read.find-all-users");

        verifyNoInteractions(userRepository);
    }


    @Test
    void isAllowedToFindUserByUUID_whenUserIsAdmin_thenReturnsTrue() {
        //GIVEN
        final UUID uuid = UUID.randomUUID();
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(true);

        //WHEN
        boolean hasPermission = userSecurityService.isAllowedToFindUserByUUID(uuid, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker, never()).hasAuthority(eq(user), anyString());

        verifyNoInteractions(userRepository);
    }


    @Test
    void isAllowedToFindUserByUUID_whenUserWithProvidedUUIDDoesNotExists_thenReturnsTrue() {
        //GIVEN
        final UUID uuid = UUID.randomUUID();
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(userRepository.findById(uuid)).thenReturn(Optional.empty());

        //WHEN
        boolean hasPermission = userSecurityService.isAllowedToFindUserByUUID(uuid, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker, never()).hasAuthority(eq(user), anyString());
        verify(userRepository).findById(uuid);
    }


    @Test
    void isAllowedToFindUserByUUID_whenUserIsNotDeactivated_thenChecksFindByUUIDPermission() {
        //GIVEN
        final UUID uuid = UUID.randomUUID();
        final User user = User.builder()
                .uuid(uuid)
                .email("user@gmail.com")
                .password("123")
                .build();

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(userRepository.findById(uuid)).thenReturn(Optional.of(user));
        when(authorityChecker.hasAuthority(user, "user.read.find-by-uuid")).thenReturn(true);

        //WHEN
        boolean hasPermission = userSecurityService.isAllowedToFindUserByUUID(uuid, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "user.read.find-by-uuid");
        verify(userRepository).findById(uuid);
    }


    @Test
    void isAllowedToFindUserByUUID_whenUserIsDeactivated_thenChecksReadDeactivatedUsersPermission() {
        //GIVEN
        final UUID uuid = UUID.randomUUID();
        final User user = User.builder()
                .uuid(uuid)
                .email("user@gmail.com")
                .password("123")
                .deactivatedAt(Instant.now())
                .build();

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(userRepository.findById(uuid)).thenReturn(Optional.of(user));

        //WHEN
        boolean hasPermission = userSecurityService.isAllowedToFindUserByUUID(uuid, user);

        //THEN
        assertThat(hasPermission).isFalse();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAllAuthorities(user, "user.read.find-by-uuid", "user.read.read-deactivated-users");
        verify(userRepository).findById(uuid);
    }


    @Test
    void isAllowedToDeleteAccount_whenTryingToDeleteOwnAccount_thenReturnsFalse() {
        //GIVEN
        final UUID uuid = UUID.fromString("4056f75f-43f9-47fa-9037-de73360c2575");
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(user.getUuid()).thenReturn(uuid);

        //WHEN
        boolean hasPermission = userSecurityService.isAllowedToDeleteAccount(uuid, user);

        //THEN
        assertThat(hasPermission).isFalse();

        verifyNoInteractions(authorityChecker, userRepository);
    }


    @Test
    void isAllowedToDeleteAccount_whenUserIsAdmin_thenReturnsTrue() {
        //GIVEN
        final UUID targetAccountUUID = UUID.fromString("4056f75f-43f9-47fa-9037-de73360c2575");
        final UUID uuid = UUID.fromString("94557ae2-bc61-4c9e-96a7-5d97c33c1e96");
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(user.getUuid()).thenReturn(uuid);
        when(authorityChecker.isAdmin(user)).thenReturn(true);

        //WHEN
        boolean hasPermission = userSecurityService.isAllowedToDeleteAccount(targetAccountUUID, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verifyNoInteractions(userRepository);
    }


    @Test
    void isAllowedToDeleteAccount_whenUserIsNotAdminButHasPermission_thenReturnsTrue() {
        //GIVEN
        final UUID targetAccountUUID = UUID.fromString("4056f75f-43f9-47fa-9037-de73360c2575");
        final UUID uuid = UUID.fromString("94557ae2-bc61-4c9e-96a7-5d97c33c1e96");
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(user.getUuid()).thenReturn(uuid);
        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(authorityChecker.hasAuthority(user, "user.delete.deactivate-by-uuid")).thenReturn(true);

        //WHEN
        boolean hasPermission = userSecurityService.isAllowedToDeleteAccount(targetAccountUUID, user);

        //THEN
        assertThat(hasPermission).isTrue();

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "user.delete.deactivate-by-uuid");
        verifyNoInteractions(userRepository);
    }


    @Test
    void chooseUserFilterStrategy_whenUserHasPermissionToReadDeactivatedUsers_thenReturnsAllUsersFilterStrategy() {
        //GIVEN
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(authorityChecker.hasAuthority(user, "user.read.read-deactivated-users")).thenReturn(true);

        //WHEN
        UserFilterStrategy userFilterStrategy = userSecurityService.chooseUserFilterStrategy(user);

        //THEN
        assertThat(userFilterStrategy).isInstanceOf(AllUserFilterStrategy.class);

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "user.read.read-deactivated-users");
        verifyNoInteractions(userRepository);
    }


    @Test
    void chooseUserFilterStrategy_whenUserHasNoPermissionToReadDeactivatedUsers_thenReturnsActiveUsersFilterStrategy() {
        //GIVEN
        final JwtAuthUserDetails user = mock(JwtAuthUserDetails.class);

        when(authorityChecker.isAdmin(user)).thenReturn(false);
        when(authorityChecker.hasAuthority(user, "user.read.read-deactivated-users")).thenReturn(false);

        //WHEN
        UserFilterStrategy userFilterStrategy = userSecurityService.chooseUserFilterStrategy(user);

        //THEN
        assertThat(userFilterStrategy).isInstanceOf(ActiveUserFilterStrategy.class);

        verify(authorityChecker).isAdmin(user);
        verify(authorityChecker).hasAuthority(user, "user.read.read-deactivated-users");
        verifyNoInteractions(userRepository);
    }
}