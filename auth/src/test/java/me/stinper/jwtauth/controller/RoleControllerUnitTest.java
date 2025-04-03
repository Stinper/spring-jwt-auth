package me.stinper.jwtauth.controller;

import jakarta.validation.ConstraintViolationException;
import me.stinper.jwtauth.dto.EntityPaginationRequest;
import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.dto.role.RoleDto;
import me.stinper.jwtauth.exception.RelatedEntityExistsException;
import me.stinper.jwtauth.service.entity.contract.RoleService;
import me.stinper.jwtauth.testutils.ConstraintViolationMockSupport;
import me.stinper.jwtauth.testutils.ServletUriComponentsBuilderMockSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for RoleController class")
@ExtendWith(MockitoExtension.class)
class RoleControllerUnitTest {
    @Mock private RoleService roleService;
    @Mock private jakarta.validation.Validator validator;

    @InjectMocks
    private RoleController roleController;


    @Test
    void findAll_whenEntityPaginationRequestValidationFails_thenThrowsException() {
        //GIVEN
        EntityPaginationRequest invalidPaginationRequest = new EntityPaginationRequest(
                0, null, null, null
        );

        final String errorMessage = "Page size must not be null";

        ConstraintViolationMockSupport.mockValidatorToReturnSingleConstraintViolation(
                validator, errorMessage, invalidPaginationRequest
        );

        //WHEN & THEN
        assertThatThrownBy(() -> roleController.findAll(invalidPaginationRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(errorMessage);

        verifyNoInteractions(roleService);
    }


    @Test
    void findAll_whenEntityPaginationRequestIsValid_thenReturnsPaginatedResult() {
        //GIVEN
        EntityPaginationRequest validEntityPaginationRequest = new EntityPaginationRequest(
                0, 10, null, null
        );

        Pageable pageable = validEntityPaginationRequest.buildPageableFromRequest();

        final RoleDto firstRole = new RoleDto(1L, "ROLE_USER"),
                secondRole = new RoleDto(2L, "ROLE_ADMIN");

        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(roleService.findAll(pageable)).thenReturn(
                new PageImpl<>(List.of(firstRole, secondRole), pageable, 2)
        );

        //WHEN
        ResponseEntity<Page<RoleDto>> result = roleController.findAll(validEntityPaginationRequest);

        //THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull()
                .hasSize(2)
                .contains(firstRole, secondRole);

        verify(roleService).findAll(pageable);
    }


    @Test
    void findByRoleName_whenRoleExists_thenReturnsRoleDto() {
        //GIVEN
        final String roleName = "ROLE_USER";
        final RoleDto roleDto = new RoleDto(1L, roleName);

        when(roleService.findByName(roleName)).thenReturn(Optional.of(roleDto));

        //WHEN
        ResponseEntity<RoleDto> result = roleController.findByRoleName(roleName);

        //THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody())
                .isNotNull()
                .isEqualTo(roleDto);

        verify(roleService).findByName(roleName);
    }


    @Test
    void findByRoleName_whenRoleDoesNotExists_thenReturns404CodeWithEmptyBody() {
        //GIVEN
        when(roleService.findByName(anyString())).thenReturn(Optional.empty());

        //WHEN
        ResponseEntity<RoleDto> result = roleController.findByRoleName("ROLE_UNKNOWN");

        //THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
    }


    @Test
    void create_whenRoleCreationRequestValidationFails_thenThrowsException() {
        //GIVEN
        RoleCreationRequest invalidRoleCreationRequest = new RoleCreationRequest(
                "MANAGER"
        );

        final String errorMessage = "Error message";

        ConstraintViolationMockSupport.mockValidatorToReturnSingleConstraintViolation(
                validator, errorMessage, invalidRoleCreationRequest
        );

        //WHEN & THEN
        assertThatThrownBy(() -> roleController.create(invalidRoleCreationRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(errorMessage);

        verifyNoInteractions(roleService);
    }


    @Test
    void create_whenRoleCreationRequestIsValid_thenReturnsResult() {
        //GIVEN
        final RoleCreationRequest roleCreationRequest = new RoleCreationRequest(
                "ROLE_USER"
        );

        final RoleDto roleDto = new RoleDto(1L, "ROLE_USER");

        final URI location = URI.create("http://localhost:8080/api/v1/jwt-auth/roles/" + roleDto.roleName());

        ServletUriComponentsBuilderMockSupport.withMockedUriComponentsBuilder(location, () -> {
            when(validator.validate(any())).thenReturn(Collections.emptySet());
            when(roleService.create(roleCreationRequest)).thenReturn(roleDto);

            //WHEN
            ResponseEntity<?> result = roleController.create(roleCreationRequest);

            //THEN
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isNull();
            assertThat(result.getHeaders().getLocation()).isEqualTo(location);

            verify(roleService).create(roleCreationRequest);
        });
    }


    @Test
    void delete_whenNoRelatedEntitiesExist_thenReturnsNoContentResponse() {
        //GIVEN
        final String roleName = "ROLE_USER";
        doNothing().when(roleService).delete(roleName);

        //WHEN
        ResponseEntity<?> result = roleController.delete(roleName);

        //THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();

        verify(roleService).delete(roleName);
    }


    @Test
    void delete_whenRelatedEntitiesExist_thenThrowsException() {
        //GIVEN
        final String roleName = "ROLE_USER";
        doThrow(RelatedEntityExistsException.class).when(roleService).delete(roleName);

        //WHEN & THEN
        assertThatExceptionOfType(RelatedEntityExistsException.class)
                .isThrownBy(() -> roleController.delete(roleName));

        verify(roleService).delete(roleName);
    }
}