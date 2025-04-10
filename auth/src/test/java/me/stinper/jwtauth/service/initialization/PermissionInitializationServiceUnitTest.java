package me.stinper.jwtauth.service.initialization;

import me.stinper.jwtauth.core.security.permission.PermissionScanner;
import me.stinper.jwtauth.entity.Permission;
import me.stinper.jwtauth.repository.PermissionRepository;
import me.stinper.jwtauth.service.initialization.contract.InitializationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for PermissionInitializationService class")
@ExtendWith(MockitoExtension.class)
class PermissionInitializationServiceUnitTest {
    @Mock private PermissionRepository permissionRepository;
    @Mock private PermissionScanner permissionScanner;

    private PermissionInitializationService permissionInitializationService;

    @BeforeEach
    void setUp() {
        this.permissionInitializationService = spy(new PermissionInitializationService(permissionRepository, permissionScanner));
    }

    @Test
    void isAlreadyInitialized_whenInitializationModeIsOnTableEmpty_thenChecksIsTableEmpty() {
        //GIVEN
        when(permissionInitializationService.getInitializationMode()).thenReturn(InitializationService.InitializationMode.ON_TABLE_EMPTY);
        when(permissionRepository.isTableEmpty()).thenReturn(true);

        //WHEN
        boolean isInitialized = permissionInitializationService.isAlreadyInitialized();

        //THEN
        assertThat(isInitialized).isFalse();

        verify(permissionRepository).isTableEmpty();
        verifyNoMoreInteractions(permissionRepository);
    }


    @Test
    void isAlreadyInitialized_whenInitializationModeIsOnReload_thenChecksScannedAndExistingPermissionDifference() throws Exception {
        //GIVEN
        final Permission firstPermission = new Permission(1L, "first.permission", null);
        final Permission secondPermission = new Permission(2L, "second.permission", "Description");

        final Set<Permission> scannedPermissions = Set.of(firstPermission, secondPermission);
        final List<Permission> existingPermissions = List.of(firstPermission, secondPermission);

        when(permissionInitializationService.getInitializationMode()).thenReturn(InitializationService.InitializationMode.ON_RELOAD);
        when(permissionScanner.resolveCandidatePackages()).thenReturn(List.of("pack"));

        //Permissions are similar in list and set -> method must return true
        when(permissionScanner.scanPermissionsFromPackage(any())).thenReturn(scannedPermissions);
        when(permissionRepository.findAll()).thenReturn(existingPermissions);

        //This @PostConstruct method MUST be called to initialized inner class fields
        permissionInitializationService.init();

        //WHEN
        boolean isInitialized = permissionInitializationService.isAlreadyInitialized();

        //THEN
        assertThat(isInitialized).isTrue();

        verify(permissionInitializationService).init();
        verify(permissionRepository).findAll();
        verifyNoMoreInteractions(permissionRepository);
        verify(permissionScanner).resolveCandidatePackages();
        verify(permissionScanner, atLeastOnce()).scanPermissionsFromPackage(any());
    }


    @Test
    void initialize_whenIsAlreadyInitializedReturnsTrue_thenReturnsNull() {
        //GIVEN
        doReturn(true).when(permissionInitializationService).isAlreadyInitialized();

        //WHEN
        List<Permission> initializedPermissions = permissionInitializationService.initialize();

        //THEN
        assertThat(initializedPermissions).isNull();

        verify(permissionInitializationService).isAlreadyInitialized();
        verifyNoInteractions(permissionRepository, permissionScanner);
    }


    @Test
    void initialize_whenIsAlreadyInitializedReturnsFalse_thenInitializesPermissionsList() throws Exception {
        //GIVEN
        final Permission firstPermission = new Permission(1L, "first.permission", null);
        final Permission secondPermission = new Permission(2L, "second.permission", "Description");

        //One permission is not initialized
        final Set<Permission> scannedPermissions = Set.of(firstPermission, secondPermission);
        final List<Permission> existingPermissions = List.of(firstPermission);

        doReturn(false).when(permissionInitializationService).isAlreadyInitialized();
        when(permissionScanner.resolveCandidatePackages()).thenReturn(List.of("pack"));

        when(permissionScanner.scanPermissionsFromPackage(any())).thenReturn(scannedPermissions);
        when(permissionRepository.findAll()).thenReturn(existingPermissions);

        when(permissionRepository.saveAll(any())).thenReturn(List.of(secondPermission));

        //This @PostConstruct method MUST be called to initialized inner class fields
        permissionInitializationService.init();

        //WHEN
        List<Permission> initializedPermissions = permissionInitializationService.initialize();

        //THEN
        assertThat(initializedPermissions).containsExactly(secondPermission);

        verify(permissionInitializationService).init();
        verify(permissionRepository).findAll();
        verifyNoMoreInteractions(permissionRepository);
        verify(permissionScanner).resolveCandidatePackages();
        verify(permissionScanner, atLeastOnce()).scanPermissionsFromPackage(any());
    }
}