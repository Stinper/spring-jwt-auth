package me.stinper.jwtauth.validation;

import me.stinper.jwtauth.core.error.RoleErrorCode;
import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.exception.ValidatorUnsupportedTypeException;
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
import org.springframework.validation.FieldError;
import org.springframework.validation.SimpleErrors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test for RoleCreationValidator class")
class RoleCreationValidatorUnitTest {
    @Mock private RoleRepository roleRepository;
    @Mock private MessageSourceHelper messageSourceHelper;

    @InjectMocks
    private RoleCreationValidator roleCreationValidator;

    private TestData testData;

    @BeforeEach
    void setUp() {
        this.testData = new TestData();
    }


    @Test
    void validate_whenTargetObjectIsNotSupported_thenThrowsException() {
        //GIVEN
        final Integer UNSUPPORTED_OBJECT = 1;

        //WHEN & THEN
        assertThatExceptionOfType(ValidatorUnsupportedTypeException.class)
                .isThrownBy(() -> roleCreationValidator.validate(UNSUPPORTED_OBJECT, testData.SIMPLE_ERRORS));

        verifyNoInteractions(roleRepository);
    }


    @Test
    void validate_whenRoleNameIsUnique_thenDoesNotRejectValue() {
        //GIVEN
        when(roleRepository.existsByRoleNameIgnoreCase(testData.ROLE_CREATION_REQUEST.roleName())).thenReturn(false);

        //WHEN
        roleCreationValidator.validate(testData.ROLE_CREATION_REQUEST, testData.SIMPLE_ERRORS);

        //THEN
        assertThat(testData.SIMPLE_ERRORS.getAllErrors()).isEmpty();
        verify(roleRepository, times(1)).existsByRoleNameIgnoreCase(testData.ROLE_CREATION_REQUEST.roleName());
    }


    @Test
    void validate_whenRoleNameIsNotUnique_thenRejectValue() {
        //GIVEN
        final String roleName = testData.ROLE_CREATION_REQUEST.roleName(), errorMessage = "ERROR_MESSAGE";

        when(roleRepository.existsByRoleNameIgnoreCase(roleName)).thenReturn(true);
        when(messageSourceHelper.getLocalizedMessage(any(), any())).thenReturn(errorMessage);

        //WHEN
        roleCreationValidator.validate(testData.ROLE_CREATION_REQUEST, testData.SIMPLE_ERRORS);

        //THEN
        assertThat(testData.SIMPLE_ERRORS.getFieldErrors())
                .hasSize(1)
                .satisfies(fieldErrors -> {
                    FieldError roleNameFieldError = fieldErrors.getFirst();

                   assertThat(roleNameFieldError.getField()).isEqualTo("roleName");
                   assertThat(roleNameFieldError.getCode()).isEqualTo(RoleErrorCode.ROLE_NAME_NOT_UNIQUE.getCode());
                   assertThat(roleNameFieldError.getDefaultMessage()).isEqualTo(errorMessage);
                });
    }


    private static class TestData {
        final RoleCreationRequest ROLE_CREATION_REQUEST = new RoleCreationRequest("ROLE_MANAGER");

        final Errors SIMPLE_ERRORS = new SimpleErrors(ROLE_CREATION_REQUEST);

    }
}
