package me.stinper.jwtauth;

import me.stinper.jwtauth.service.initialization.AdminAccountInitializationService;
import me.stinper.jwtauth.service.initialization.PermissionInitializationService;
import me.stinper.jwtauth.service.initialization.RoleInitializationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.List;

import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for InitializationApplicationRunner class")
@ExtendWith(MockitoExtension.class)
class InitializationApplicationRunnerUnitTest {
    @Mock private RoleInitializationService roleInitializationService;
    @Mock private AdminAccountInitializationService adminAccountInitializationService;
    @Mock private PermissionInitializationService permissionInitializationService;

    private InitializationApplicationRunner initializationApplicationRunner;

    @BeforeEach
    void setUp() {
        this.initializationApplicationRunner = new InitializationApplicationRunner(
                List.of(roleInitializationService, adminAccountInitializationService, permissionInitializationService)
        );
    }


    @Test
    void run_invokesAllInitializationServices() throws Exception {
        //WHEN
        initializationApplicationRunner.run(mock(ApplicationArguments.class));

        //THEN
        verify(roleInitializationService).initialize();
        verify(adminAccountInitializationService).initialize();
        verify(permissionInitializationService).initialize();

        verifyNoMoreInteractions(roleInitializationService, adminAccountInitializationService, permissionInitializationService);
    }
}