package me.stinper.jwtauth.service.initialization;

import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.repository.RoleRepository;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.service.initialization.contract.InitializationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for AdminAccountInitializationService class")
class AdminAccountInitializationServiceUnitTest {
    @Mock private InitializationService<Role> roleInitializationService;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    private final String adminAccountEmail = "admin@gmail.com";
    private final String adminAccountPassword = "123";
    private final String adminRoleName = "ROLE_ADMIN";

    private AdminAccountInitializationService adminAccountInitializationService;

    @BeforeEach
    void setUp() {
        this.adminAccountInitializationService = spy(new AdminAccountInitializationService(
                roleInitializationService,
                userRepository,
                roleRepository,
                passwordEncoder
        ));

        adminAccountInitializationService.setAdminAccountEmail(adminAccountEmail);
        adminAccountInitializationService.setAdminAccountPassword(adminAccountPassword);
        adminAccountInitializationService.setAdminRoleName(adminRoleName);
    }


    @Test
    void isAlreadyInitialized_whenInitializationModeIsOnTableEmpty_thenChecksIsTableEmpty() {
        //GIVEN
        when(adminAccountInitializationService.getInitializationMode()).thenReturn(InitializationService.InitializationMode.ON_TABLE_EMPTY);
        when(userRepository.isTableEmpty()).thenReturn(true);

        //WHEN
        boolean isInitialized = adminAccountInitializationService.isAlreadyInitialized();

        //THEN
        assertThat(isInitialized).isFalse();

        verify(userRepository).isTableEmpty();
        verify(userRepository, never()).existsByEmailIgnoreCase(any());
    }


    @Test
    void isAlreadyInitialized_whenInitializationModeIsOnTableEmpty_thenChecksIsUserExist() {
        //GIVEN
        when(adminAccountInitializationService.getInitializationMode()).thenReturn(InitializationService.InitializationMode.ON_RELOAD);
        when(userRepository.existsByEmailIgnoreCase(this.adminAccountEmail)).thenReturn(false);

        //WHEN
        boolean isInitialized = adminAccountInitializationService.isAlreadyInitialized();

        //THEN
        assertThat(isInitialized).isFalse();

        verify(userRepository, never()).isTableEmpty();
        verify(userRepository).existsByEmailIgnoreCase(this.adminAccountEmail);
    }


    @Test
    void isAlreadyInitialized_whenInitializationModeIsOnTableEmptyAndTableIsNotEmpty_thenReturnsTrue() {
        //GIVEN
        when(this.adminAccountInitializationService.getInitializationMode())
                .thenReturn(InitializationService.InitializationMode.ON_TABLE_EMPTY);
        when(userRepository.isTableEmpty()).thenReturn(false); //Table is NOT empty

        //WHEN
        boolean isInitialized = adminAccountInitializationService.isAlreadyInitialized();

        //THEN
        assertThat(isInitialized).isTrue(); //Result is true = user already initialized
        verify(userRepository).isTableEmpty();
    }


    @Test
    void isAlreadyInitialized_whenInitializationModeIsOnReloadAndRoleAlreadyExists_thenReturnsTrue() {
        //GIVEN
        when(this.adminAccountInitializationService.getInitializationMode())
                .thenReturn(InitializationService.InitializationMode.ON_RELOAD);
        when(userRepository.existsByEmailIgnoreCase(this.adminAccountEmail)).thenReturn(true); //User already exists

        //WHEN
        boolean isInitialized = adminAccountInitializationService.isAlreadyInitialized();

        //THEN
        assertThat(isInitialized).isTrue(); //Result is true = user already initialized
        verify(userRepository).existsByEmailIgnoreCase(this.adminAccountEmail);
    }


    @Test
    void initialize_whenIsAlreadyInitializedReturnsTrue_thenReturnsNull() {
        //GIVEN
        when(adminAccountInitializationService.isAlreadyInitialized()).thenReturn(true);

        //WHEN
        User initializedUser = adminAccountInitializationService.initialize();

        //THEN
        assertThat(initializedUser).isNull();

        verify(adminAccountInitializationService, atLeastOnce()).isAlreadyInitialized();
        verify(userRepository, never()).save(any());
    }


    @Test
    void initialize_whenAdminRoleSuccessfullyInitialized_thenNeverCallRoleRepository() {
        //GIVEN
        when(adminAccountInitializationService.isAlreadyInitialized()).thenReturn(false);

        when(roleInitializationService.initialize()).thenReturn(TestData.ADMIN_ROLE);
        when(passwordEncoder.encode(this.adminAccountPassword)).thenReturn(TestData.HASHED_PASSWORD);
        when(userRepository.save(any())).thenReturn(TestData.ADMIN_USER);

        //WHEN
        User user = adminAccountInitializationService.initialize();

        //THEN
        assertThat(user).isEqualTo(TestData.ADMIN_USER);

        verify(roleInitializationService).initialize();
        verify(roleRepository, never()).findByRoleNameIgnoreCase(any());
        verify(passwordEncoder).encode(this.adminAccountPassword);
        verify(userRepository).save(any());
    }


    @Test
    void initialize_whenAdminRoleInitializationServiceReturnsNull_thenCallRoleRepositoryToTryFindRole() {
        //GIVEN
        when(adminAccountInitializationService.isAlreadyInitialized()).thenReturn(false);

        when(roleInitializationService.initialize()).thenReturn(null);
        when(roleRepository.findByRoleNameIgnoreCase(this.adminRoleName)).thenReturn(Optional.of(TestData.ADMIN_ROLE));
        when(passwordEncoder.encode(this.adminAccountPassword)).thenReturn(TestData.HASHED_PASSWORD);
        when(userRepository.save(any())).thenReturn(TestData.ADMIN_USER);

        //WHEN
        User user = adminAccountInitializationService.initialize();

        //THEN
        assertThat(user).isEqualTo(TestData.ADMIN_USER);

        verify(roleInitializationService).initialize();
        verify(roleRepository).findByRoleNameIgnoreCase(this.adminRoleName);
        verify(passwordEncoder).encode(this.adminAccountPassword);
        verify(userRepository).save(any());
    }


    @Test
    void initialize_whenAdminRoleInitializationServiceReturnsNullAndAdminRoleNotFound_thenAdminAccountContainsEmptyRolesList() {
        //GIVEN
        when(adminAccountInitializationService.isAlreadyInitialized()).thenReturn(false);

        when(roleInitializationService.initialize()).thenReturn(null);
        when(roleRepository.findByRoleNameIgnoreCase(this.adminRoleName)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(this.adminAccountPassword)).thenReturn(TestData.HASHED_PASSWORD);

        final User adminUser = User
                .builder()
                .uuid(UUID.randomUUID())
                .email("admin@gmail.com")
                .password(TestData.HASHED_PASSWORD)
                .isEmailVerified(true)
                .roles(Collections.emptySet())
                .build();

        when(userRepository.save(any())).thenReturn(adminUser);

        //WHEN
        User user = adminAccountInitializationService.initialize();

        //THEN
        assertThat(user).isEqualTo(adminUser);

        verify(roleInitializationService).initialize();
        verify(roleRepository).findByRoleNameIgnoreCase(this.adminRoleName);
        verify(passwordEncoder).encode(this.adminAccountPassword);
        verify(userRepository).save(any());
    }


    private static class TestData {
        static final Role ADMIN_ROLE = new Role(1L, "ROLE_ADMIN", "Администратор", Collections.emptySet());
        static final String HASHED_PASSWORD = "$HASHED_PASSWORD$";
        static final User ADMIN_USER = User
                .builder()
                .uuid(UUID.randomUUID())
                .email("admin@gmail.com")
                .password(HASHED_PASSWORD)
                .isEmailVerified(true)
                .roles(Set.of(ADMIN_ROLE))
                .build();

    }

}