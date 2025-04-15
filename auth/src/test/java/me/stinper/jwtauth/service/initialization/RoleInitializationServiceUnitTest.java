package me.stinper.jwtauth.service.initialization;

import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.repository.RoleRepository;
import me.stinper.jwtauth.service.initialization.contract.InitializationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for RoleInitializationService class")
class RoleInitializationServiceUnitTest {
    @Mock private RoleRepository roleRepository;
    private final String adminRoleName = "ROLE_ADMIN";
    private final String adminRolePrefix = "Администратор";

    private RoleInitializationService roleInitializationService;

    @BeforeEach
    void setUp() {
        this.roleInitializationService = spy(new RoleInitializationService(roleRepository));

        roleInitializationService.setAdminRoleName(adminRoleName);
        roleInitializationService.setAdminRolePrefix(adminRolePrefix);
    }

    @Test
    void isAlreadyInitialized_whenInitializationModeIsOnTableEmpty_thenChecksIsTableEmpty() {
        //GIVEN
        when(this.roleInitializationService.getInitializationMode()).thenReturn(InitializationService.InitializationMode.ON_TABLE_EMPTY);
        when(roleRepository.isTableEmpty()).thenReturn(true);

        //WHEN
        boolean isInitialized = roleInitializationService.isAlreadyInitialized();

        //THEN
        assertThat(isInitialized).isFalse();
        verify(roleRepository).isTableEmpty();
        verify(roleRepository, never()).existsByRoleNameIgnoreCase(any());
    }

    @Test
    void isAlreadyInitialized_whenInitializationModeIsOnReload_thenChecksIsRoleExist() {
        //GIVEN
        when(this.roleInitializationService.getInitializationMode()).thenReturn(InitializationService.InitializationMode.ON_RELOAD);
        when(roleRepository.existsByRoleNameIgnoreCase(this.adminRoleName)).thenReturn(false);

        //WHEN
        boolean isInitialized = roleInitializationService.isAlreadyInitialized();

        //THEN
        assertThat(isInitialized).isFalse();
        verify(roleRepository, never()).isTableEmpty();
        verify(roleRepository).existsByRoleNameIgnoreCase(this.adminRoleName);
    }

    @Test
    void isAlreadyInitialized_whenInitializationModeIsOnTableEmptyAndTableIsNotEmpty_thenReturnsTrue() {
        //GIVEN
        when(this.roleInitializationService.getInitializationMode()).thenReturn(InitializationService.InitializationMode.ON_TABLE_EMPTY);
        when(roleRepository.isTableEmpty()).thenReturn(false); //Table is NOT empty

        //WHEN
        boolean isInitialized = roleInitializationService.isAlreadyInitialized();

        //THEN
        assertThat(isInitialized).isTrue(); //Result is true = role already initialized
        verify(roleRepository).isTableEmpty();
    }


    @Test
    void isAlreadyInitialized_whenInitializationModeIsOnReloadAndRoleAlreadyExists_thenReturnsTrue() {
        //GIVEN
        when(this.roleInitializationService.getInitializationMode()).thenReturn(InitializationService.InitializationMode.ON_RELOAD);
        when(roleRepository.existsByRoleNameIgnoreCase(this.adminRoleName)).thenReturn(true); //Role already exists

        //WHEN
        boolean isInitialized = roleInitializationService.isAlreadyInitialized();

        //THEN
        assertThat(isInitialized).isTrue(); //Result is true = role already initialized
        verify(roleRepository).existsByRoleNameIgnoreCase(this.adminRoleName);
    }


    @Test
    void initialize_whenIsAlreadyInitializedReturnsTrue_thenReturnsNull() {
        //GIVEN
        when(roleInitializationService.isAlreadyInitialized()).thenReturn(true);

        //WHEN
        Role adminRole = roleInitializationService.initialize();

        //THEN
        assertThat(adminRole).isNull();
        verify(roleInitializationService, atLeastOnce()).isAlreadyInitialized();
        verify(roleRepository, never()).save(any());
    }

    @Test
    void initialize_whenIsAlreadyInitializedReturnsFalse_thenInitializeRole() {
        //GIVEN
        final Role adminRole = new Role(1L, this.adminRoleName, this.adminRolePrefix, Collections.emptySet());

        when(roleInitializationService.isAlreadyInitialized()).thenReturn(false);
        when(roleRepository.save(any())).thenReturn(adminRole);

        //WHEN
        Role initializedRole = roleInitializationService.initialize();

        //THEN
        assertThat(initializedRole).isEqualTo(adminRole);
        verify(roleRepository).save(argThat(role -> role.getRoleName().equals(this.adminRoleName)));
    }

}