package me.stinper.jwtauth.controller;

import jakarta.validation.ConstraintViolationException;
import me.stinper.jwtauth.dto.EntityPaginationRequest;
import me.stinper.jwtauth.dto.permission.PermissionCreationRequest;
import me.stinper.jwtauth.dto.permission.PermissionDescriptionUpdateRequest;
import me.stinper.jwtauth.dto.permission.PermissionDto;
import me.stinper.jwtauth.exception.RelatedEntityExistsException;
import me.stinper.jwtauth.exception.ResourceNotFoundException;
import me.stinper.jwtauth.service.entity.contract.PermissionService;
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

@DisplayName("Unit Tests for PermissionController class")
@ExtendWith(MockitoExtension.class)
class PermissionControllerUnitTest {
    @Mock private PermissionService permissionService;
    @Mock private jakarta.validation.Validator validator;

    @InjectMocks
    private PermissionController permissionController;


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
        assertThatThrownBy(() -> permissionController.findAll(invalidPaginationRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(errorMessage);

        verify(validator).validate(invalidPaginationRequest);
        verifyNoInteractions(permissionService);
    }


    @Test
    void findAll_whenEntityPaginationRequestIsValid_thenReturnsPaginatedResult() {
        //GIVEN
        EntityPaginationRequest validEntityPaginationRequest = new EntityPaginationRequest(
                0, 10, null, null
        );

        Pageable pageable = validEntityPaginationRequest.buildPageableFromRequest();

        final PermissionDto firstPermission = new PermissionDto(1L, "permission.read", "Read permission"),
                secondPermission = new PermissionDto(2L, "permission.write", "Write permission");

        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(permissionService.findAll(pageable)).thenReturn(
                new PageImpl<>(List.of(firstPermission, secondPermission), pageable, 2)
        );

        //WHEN
        ResponseEntity<Page<PermissionDto>> result = permissionController.findAll(validEntityPaginationRequest);

        //THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull()
                .hasSize(2)
                .contains(firstPermission, secondPermission);

        verify(validator).validate(validEntityPaginationRequest);
        verify(permissionService).findAll(pageable);
    }


    @Test
    void findById_whenPermissionExists_thenReturnsPermissionDto() {
        //GIVEN
        final Long permissionId = 1L;
        final PermissionDto permissionDto = new PermissionDto(permissionId, "permission.read", "Read permission");

        when(permissionService.findById(permissionId)).thenReturn(Optional.of(permissionDto));

        //WHEN
        ResponseEntity<?> result = permissionController.findById(permissionId);

        //THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody())
                .isNotNull()
                .isEqualTo(permissionDto);

        verify(permissionService).findById(permissionId);
    }


    @Test
    void findById_whenPermissionDoesNotExists_thenReturns404CodeWithEmptyBody() {
        //GIVEN
        final Long permissionId = 999L;
        when(permissionService.findById(permissionId)).thenReturn(Optional.empty());

        //WHEN
        ResponseEntity<?> result = permissionController.findById(permissionId);

        //THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();

        verify(permissionService).findById(permissionId);
    }


    @Test
    void create_whenPermissionCreationRequestValidationFails_thenThrowsException() {
        //GIVEN
        PermissionCreationRequest invalidPermissionCreationRequest = new PermissionCreationRequest(
                "invalid", "Invalid permission"
        );

        final String errorMessage = "Error message";

        ConstraintViolationMockSupport.mockValidatorToReturnSingleConstraintViolation(
                validator, errorMessage, invalidPermissionCreationRequest
        );

        //WHEN & THEN
        assertThatThrownBy(() -> permissionController.create(invalidPermissionCreationRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(errorMessage);

        verify(validator).validate(invalidPermissionCreationRequest);
        verifyNoInteractions(permissionService);
    }


    @Test
    void create_whenPermissionCreationRequestIsValid_thenReturnsResult() {
        //GIVEN
        final PermissionCreationRequest permissionCreationRequest = new PermissionCreationRequest(
                "permission.read", "Read permission"
        );

        final PermissionDto permissionDto = new PermissionDto(1L, "permission.read", "Read permission");

        final URI location = URI.create("http://localhost:8080/api/v1/jwt-auth/permissions/" + permissionDto.id());

        ServletUriComponentsBuilderMockSupport.withMockedUriComponentsBuilder(location, () -> {
            when(validator.validate(any())).thenReturn(Collections.emptySet());
            when(permissionService.create(permissionCreationRequest)).thenReturn(permissionDto);

            //WHEN
            ResponseEntity<PermissionDto> result = permissionController.create(permissionCreationRequest);

            //THEN
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isEqualTo(permissionDto);
            assertThat(result.getHeaders().getLocation()).isEqualTo(location);

            verify(validator).validate(permissionCreationRequest);
            verify(permissionService).create(permissionCreationRequest);
        });
    }


    @Test
    void updateDescription_whenPermissionDescriptionUpdateRequestValidationFails_thenThrowsException() {
        //GIVEN
        final PermissionDescriptionUpdateRequest permissionDescriptionUpdateRequest = new PermissionDescriptionUpdateRequest(
                "Updated description"
        );

        final String errorMessage = "Error Message";

        ConstraintViolationMockSupport.mockValidatorToReturnSingleConstraintViolation(
                validator, errorMessage, permissionDescriptionUpdateRequest
        );

        //WHEN & THEN
        assertThatThrownBy(() -> permissionController.updateDescription(1L, permissionDescriptionUpdateRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(errorMessage);

        verify(validator).validate(permissionDescriptionUpdateRequest);
        verifyNoInteractions(permissionService);
    }


    @Test
    void updateDescription_whenPermissionDoesNotExists_thenThrowsException() {
        //GIVEN
        final Long permissionId = 999L;

        final PermissionDescriptionUpdateRequest permissionDescriptionUpdateRequest = new PermissionDescriptionUpdateRequest(
                "Updated description"
        );

        final String newDescription = permissionDescriptionUpdateRequest.description();

        when(validator.validate(permissionDescriptionUpdateRequest)).thenReturn(Collections.emptySet());
        when(permissionService.updateDescription(permissionId, newDescription)).thenThrow(ResourceNotFoundException.class);

        //WHEN & THEN
        assertThatThrownBy(() -> permissionController.updateDescription(permissionId, permissionDescriptionUpdateRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(validator).validate(permissionDescriptionUpdateRequest);
        verify(permissionService).updateDescription(permissionId, newDescription);
    }


    @Test
    void updateDescription_whenPermissionExists_thenReturnsUpdatedPermissionDto() {
        //GIVEN
        final Long permissionId = 1L;

        final PermissionDescriptionUpdateRequest permissionDescriptionUpdateRequest = new PermissionDescriptionUpdateRequest(
                "Updated description"
        );

        final String newDescription = permissionDescriptionUpdateRequest.description();

        final PermissionDto updatedPermission = new PermissionDto(
                permissionId, "permission.read", newDescription
        );

        when(validator.validate(permissionDescriptionUpdateRequest)).thenReturn(Collections.emptySet());
        when(permissionService.updateDescription(permissionId, newDescription)).thenReturn(updatedPermission);

        //WHEN
        ResponseEntity<PermissionDto> response = permissionController.updateDescription(permissionId, permissionDescriptionUpdateRequest);

        //THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(updatedPermission);

        verify(validator).validate(permissionDescriptionUpdateRequest);
        verify(permissionService).updateDescription(permissionId, newDescription);
    }


    @Test
    void delete_whenNoRelatedEntitiesExist_thenReturnsNoContentResponse() {
        //GIVEN
        final Long permissionId = 1L;
        doNothing().when(permissionService).delete(permissionId);

        //WHEN
        ResponseEntity<?> result = permissionController.delete(permissionId);

        //THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();

        verify(permissionService).delete(permissionId);
    }


    @Test
    void delete_whenRelatedEntitiesExist_thenThrowsException() {
        //GIVEN
        final Long permissionId = 1L;
        doThrow(RelatedEntityExistsException.class).when(permissionService).delete(permissionId);

        //WHEN & THEN
        assertThatExceptionOfType(RelatedEntityExistsException.class)
                .isThrownBy(() -> permissionController.delete(permissionId));

        verify(permissionService).delete(permissionId);
    }
}