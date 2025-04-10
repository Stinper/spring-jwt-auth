package me.stinper.jwtauth.validation;

import me.stinper.jwtauth.core.error.PermissionErrorCode;
import me.stinper.jwtauth.dto.permission.PermissionCreationRequest;
import me.stinper.jwtauth.exception.ValidatorUnsupportedTypeException;
import me.stinper.jwtauth.repository.PermissionRepository;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for PermissionCreationValidator class")
@ExtendWith(MockitoExtension.class)
class PermissionCreationValidatorUnitTest {
    @Mock private PermissionRepository permissionRepository;
    @Mock private MessageSourceHelper messageSourceHelper;

    @InjectMocks
    private PermissionCreationValidator permissionCreationValidator;

    @Test
    void validate_whenTargetObjectIsNotSupported_thenThrowsException() {
        //WHEN & THEN
        assertThatExceptionOfType(ValidatorUnsupportedTypeException.class)
                .isThrownBy(() -> permissionCreationValidator.validate(1, mock(Errors.class)));

        verifyNoInteractions(permissionRepository, messageSourceHelper);
    }


    @Test
    void validate_whenPermissionIsUnique_thenDoesNotRejectValue() {
        //GIVEN
        final PermissionCreationRequest permissionCreationRequest = new PermissionCreationRequest(
                "some.permission", "description"
        );

        final Errors errors = new SimpleErrors(permissionCreationRequest);
        when(permissionRepository.existsByPermissionIgnoreCase(permissionCreationRequest.permission())).thenReturn(false);

        //WHEN
        permissionCreationValidator.validate(permissionCreationRequest, errors);

        //THEN
        assertThat(errors.getAllErrors()).isEmpty();

        verify(permissionRepository).existsByPermissionIgnoreCase(permissionCreationRequest.permission());
        verifyNoInteractions(messageSourceHelper);
    }


    @Test
    void validate_whenPermissionIsNotUnique_thenRejectsValue() {
        //GIVEN
        final PermissionCreationRequest permissionCreationRequest = new PermissionCreationRequest(
                "some.permission", "description"
        );

        final Errors errors = new SimpleErrors(permissionCreationRequest);
        final String errorMessage = "Error Message";

        when(permissionRepository.existsByPermissionIgnoreCase(permissionCreationRequest.permission())).thenReturn(true);
        when(messageSourceHelper.getLocalizedMessage(any(), any())).thenReturn(errorMessage);

        //WHEN
        permissionCreationValidator.validate(permissionCreationRequest, errors);

        //THEN
        assertThat(errors.getFieldErrors())
                .hasSize(1)
                .first()
                .satisfies(fieldError -> {
                    assertThat(fieldError.getField()).isEqualTo("permission");
                    assertThat(fieldError.getCode()).isEqualTo(PermissionErrorCode.PERMISSION_NOT_UNIQUE.getCode());
                    assertThat(fieldError.getDefaultMessage()).isEqualTo(errorMessage);
                });

        verify(permissionRepository).existsByPermissionIgnoreCase(permissionCreationRequest.permission());
        verify(messageSourceHelper).getLocalizedMessage(any(), any());
    }
}