package me.stinper.jwtauth.validation;

import me.stinper.jwtauth.core.error.RoleErrorCode;
import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.entity.Permission;
import me.stinper.jwtauth.exception.ValidatorUnsupportedTypeException;
import me.stinper.jwtauth.repository.PermissionRepository;
import me.stinper.jwtauth.repository.RoleRepository;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test for RoleCreationValidator class")
class RoleCreationValidatorUnitTest {
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private MessageSourceHelper messageSourceHelper;

    private RoleCreationValidator roleCreationValidator;

    @BeforeEach
    void setUp() {
        this.roleCreationValidator = spy(new RoleCreationValidator(roleRepository, permissionRepository, messageSourceHelper));
    }

    @Test
    void validate_whenTargetObjectIsNotSupported_thenThrowsException() {
        //WHEN & THEN
        assertThatExceptionOfType(ValidatorUnsupportedTypeException.class)
                .isThrownBy(() -> roleCreationValidator.validate(1, mock(Errors.class)));

        verifyNoInteractions(roleRepository, permissionRepository, messageSourceHelper);
    }


    @Test
    void validate_whenRoleNameIsUnique_thenDoesNotRejectValue() {
        //GIVEN
        final RoleCreationRequest roleCreationRequest = new RoleCreationRequest(
                "ROLE_MANAGER", "Менеджер", Collections.emptySet()
        );

        final Errors errors = new SimpleErrors(roleCreationRequest);

        when(roleRepository.existsByRoleNameIgnoreCase(roleCreationRequest.roleName())).thenReturn(false);

        //WHEN
        roleCreationValidator.validate(roleCreationRequest, errors);

        //THEN
        assertThat(errors.getAllErrors()).isEmpty();

        verify(roleRepository).existsByRoleNameIgnoreCase(roleCreationRequest.roleName());
        verify(roleCreationValidator, never()).validateInputPermissions(any(), any());
    }


    @Test
    void validate_whenRoleNameIsNotUnique_thenRejectValue() {
        //GIVEN
        final RoleCreationRequest roleCreationRequest = new RoleCreationRequest(
                "ROLE_MANAGER", "Менеджер", Collections.emptySet()
        );

        final Errors errors = new SimpleErrors(roleCreationRequest);

        final String errorMessage = "ERROR_MESSAGE";

        when(roleRepository.existsByRoleNameIgnoreCase(roleCreationRequest.roleName())).thenReturn(true);
        when(messageSourceHelper.getLocalizedMessage(any(), any())).thenReturn(errorMessage);

        //WHEN
        roleCreationValidator.validate(roleCreationRequest, errors);

        //THEN
        assertThat(errors.getFieldErrors())
                .hasSize(1)
                .first()
                .satisfies(fieldError -> {
                    assertThat(fieldError.getField()).isEqualTo("roleName");
                    assertThat(fieldError.getCode()).isEqualTo(RoleErrorCode.ROLE_NAME_NOT_UNIQUE.getCode());
                    assertThat(fieldError.getDefaultMessage()).isEqualTo(errorMessage);
                });

        verify(messageSourceHelper).getLocalizedMessage(any(), any());
        verify(roleRepository).existsByRoleNameIgnoreCase(roleCreationRequest.roleName());
        verify(roleCreationValidator, never()).validateInputPermissions(any(), any());
    }


    @Test
    void validate_whenRoleNameIsUniqueAndCreationRequestContainsPermission_thenCallsPermissionValidationMethod() {
        //GIVEN
        final RoleCreationRequest roleCreationRequest = new RoleCreationRequest(
                "ROLE_MANAGER", "Менеджер", Set.of("some.permission", "some.another.permission")
        );

        final Errors errors = new SimpleErrors(roleCreationRequest);

        when(roleRepository.existsByRoleNameIgnoreCase(roleCreationRequest.roleName())).thenReturn(false);
        doNothing().when(roleCreationValidator).validateInputPermissions(roleCreationRequest.permissions(), errors);

        //WHEN
        roleCreationValidator.validate(roleCreationRequest, errors);

        //THEN
        assertThat(errors.getAllErrors()).isEmpty();

        verify(roleRepository).existsByRoleNameIgnoreCase(roleCreationRequest.roleName());
        verify(roleCreationValidator).validateInputPermissions(roleCreationRequest.permissions(), errors);
    }


    @Test
    void validateInputPermissions_whenPermissionSetContainsNonExistentPermission_thenRejectsValue() {
        //GIVEN
        final RoleCreationRequest roleCreationRequest = new RoleCreationRequest(
                "ROLE_MANAGER", "Менеджер", Set.of("some.permission", "some.another.permission")
        );

        final Errors errors = new SimpleErrors(roleCreationRequest);
        final Set<String> permissions = roleCreationRequest.permissions();
        final List<Permission> existingPermissions = List.of(
                new Permission(1L, "some.permission", null)
        );

        final String errorMessage = "ERROR_MESSAGE";

        when(messageSourceHelper.getLocalizedMessage(any(), any())).thenReturn(errorMessage);
        when(permissionRepository.findAllByPermissionIn(permissions)).thenReturn(existingPermissions);

        //WHEN
        roleCreationValidator.validateInputPermissions(permissions, errors);

        //THEN
        assertThat(errors.getFieldErrors())
                .hasSize(1)
                .first()
                .satisfies(fieldError -> {
                    assertThat(fieldError.getField()).isEqualTo("permissions");
                    assertThat(fieldError.getCode()).isEqualTo(RoleErrorCode.INVALID_PERMISSION_CODE.getCode());
                    assertThat(fieldError.getDefaultMessage()).isEqualTo(errorMessage);
                });

        verify(messageSourceHelper).getLocalizedMessage(any(), any());
        verify(roleCreationValidator).validateInputPermissions(permissions, errors);
        verify(permissionRepository).findAllByPermissionIn(permissions);

        verifyNoInteractions(roleRepository);
    }


    @Test
    void validateInputPermissions_whenPermissionSetIsValid_thenDoesNotRejectValue() {
        //GIVEN
        final RoleCreationRequest roleCreationRequest = new RoleCreationRequest(
                "ROLE_MANAGER", "Менеджер", Set.of("some.permission")
        );

        final Errors errors = new SimpleErrors(roleCreationRequest);
        final Set<String> permissions = roleCreationRequest.permissions();
        final List<Permission> existingPermissions = List.of(
                new Permission(1L, "some.permission", null)
        );

        when(permissionRepository.findAllByPermissionIn(permissions)).thenReturn(existingPermissions);

        //WHEN
        roleCreationValidator.validateInputPermissions(permissions, errors);

        //THEN
        assertThat(errors.getAllErrors()).isEmpty();

        verify(roleCreationValidator).validateInputPermissions(permissions, errors);
        verify(permissionRepository).findAllByPermissionIn(permissions);
        verifyNoInteractions(roleRepository, messageSourceHelper);
    }
}
