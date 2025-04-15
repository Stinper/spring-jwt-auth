package me.stinper.jwtauth.service.entity;

import me.stinper.jwtauth.dto.EntityPaginationRequest;
import me.stinper.jwtauth.dto.permission.PermissionCreationRequest;
import me.stinper.jwtauth.dto.permission.PermissionDto;
import me.stinper.jwtauth.entity.Permission;
import me.stinper.jwtauth.exception.EntityValidationException;
import me.stinper.jwtauth.exception.NoSuchPropertyException;
import me.stinper.jwtauth.exception.RelatedEntityExistsException;
import me.stinper.jwtauth.exception.ResourceNotFoundException;
import me.stinper.jwtauth.mapping.PermissionMapper;
import me.stinper.jwtauth.repository.PermissionRepository;
import me.stinper.jwtauth.repository.RoleRepository;
import me.stinper.jwtauth.validation.PermissionCreationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for PermissionServiceImpl class")
@ExtendWith(MockitoExtension.class)
class PermissionServiceImplUnitTest {
    @Mock private PermissionRepository permissionRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PermissionCreationValidator permissionCreationValidator;
    @Mock private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private TestData testData;

    @BeforeEach
    void setUp() {
        this.testData = new TestData();
    }

    @Test
    void findAll_returnsCorrectRecordsPage() {
        //GIVEN
        when(permissionMapper.toPermissionDto(testData.READ_PERMISSION)).thenReturn(testData.READ_PERMISSION_DTO);
        when(permissionMapper.toPermissionDto(testData.WRITE_PERMISSION)).thenReturn(testData.WRITE_PERMISSION_DTO);
        when(permissionMapper.toPermissionDto(testData.DELETE_PERMISSION)).thenReturn(testData.DELETE_PERMISSION_DTO);

        when(permissionRepository.findAll(testData.NO_SORT_PAGINATION_REQUEST)).thenReturn(
                new PageImpl<>(
                        List.of(testData.READ_PERMISSION, testData.WRITE_PERMISSION, testData.DELETE_PERMISSION),
                        testData.NO_SORT_PAGINATION_REQUEST,
                        3
                )
        );

        //WHEN
        Page<PermissionDto> permissions = permissionService.findAll(testData.NO_SORT_PAGINATION_REQUEST);

        //THEN
        assertThat(permissions).hasSize(3).containsAll(
                List.of(testData.READ_PERMISSION_DTO, testData.WRITE_PERMISSION_DTO, testData.DELETE_PERMISSION_DTO)
        );

        verify(permissionRepository).findAll(testData.NO_SORT_PAGINATION_REQUEST);
    }

    @Test
    void findAll_whenSortByNotExistentProperty_thenThrowsException() {
        //GIVEN
        when(permissionRepository.findAll(any(Pageable.class)))
                .thenThrow(
                        new PropertyReferenceException("nonExistentProperty", TypeInformation.OBJECT, Collections.emptyList())
                );

        //WHEN & THEN
        assertThatExceptionOfType(NoSuchPropertyException.class)
                .isThrownBy(() -> permissionService.findAll(Pageable.unpaged()))
                .satisfies(ex -> assertThat(
                        ex.getPropertyReferenceException().getPropertyName()).isEqualTo("nonExistentProperty")
                );
    }

    @Test
    void findById_whenCorrectId_thenReturnsPermission() {
        //GIVEN
        final Long permissionId = 1L;

        when(permissionMapper.toPermissionDto(testData.READ_PERMISSION)).thenReturn(testData.READ_PERMISSION_DTO);
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testData.READ_PERMISSION));

        //WHEN
        Optional<PermissionDto> permissionDto = permissionService.findById(permissionId);

        //THEN
        assertThat(permissionDto).hasValue(testData.READ_PERMISSION_DTO);

        verify(permissionRepository).findById(permissionId);
    }

    @Test
    void findById_whenIncorrectId_thenReturnsEmptyOptional() {
        //GIVEN
        final Long permissionId = 999L;
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

        //WHEN
        Optional<PermissionDto> permissionDto = permissionService.findById(permissionId);

        //THEN
        assertThat(permissionDto).isEmpty();

        verify(permissionRepository).findById(permissionId);
    }

    @Test
    void create_whenCreationRequestIsValid_thenCreatesRecord() {
        //GIVEN
        final String permissionName = testData.READ_PERMISSION.getPermission();

        PermissionCreationRequest permissionCreationRequest = new PermissionCreationRequest(
                permissionName, "Право на чтение"
        );

        when(permissionMapper.toPermission(permissionCreationRequest)).thenReturn(Permission.builder().build());
        when(permissionMapper.toPermissionDto(testData.READ_PERMISSION)).thenReturn(testData.READ_PERMISSION_DTO);

        when(permissionRepository.save(any())).thenReturn(testData.READ_PERMISSION);
        when(permissionCreationValidator.validateObject(permissionCreationRequest)).thenReturn(new SimpleErrors(permissionCreationRequest));

        //WHEN
        PermissionDto permissionDto = permissionService.create(permissionCreationRequest);

        //THEN
        assertThat(permissionDto).isEqualTo(testData.READ_PERMISSION_DTO);

        verify(permissionRepository).save(any());
    }

    @Test
    void create_whenPermissionCreationRequestNotValid_thenThrowsException() {
        //GIVEN
        PermissionCreationRequest invalidPermissionCreationRequest = new PermissionCreationRequest(
                "INVALID", "Description"
        );

        Errors permissionCreationRequestErrors = new SimpleErrors(invalidPermissionCreationRequest);
        permissionCreationRequestErrors.rejectValue("permission", "code");

        when(permissionCreationValidator.validateObject(invalidPermissionCreationRequest)).thenReturn(permissionCreationRequestErrors);

        //WHEN & THEN
        assertThatExceptionOfType(EntityValidationException.class)
                .isThrownBy(() -> permissionService.create(invalidPermissionCreationRequest));

        verify(permissionRepository, never()).save(any());
    }

    @Test
    void updateDescription_whenPermissionFound_thenUpdatesDescription() {
        //GIVEN
        final Long permissionId = 1L;
        final String newDescription = "Новое описание";

        Permission existingPermission = new Permission(permissionId, "user.read", "Право на чтение пользователей");
        Permission updatedPermission = new Permission(permissionId, "user.read", newDescription);

        PermissionDto updatedPermissionDto = new PermissionDto(permissionId, "user.read", newDescription);

        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(existingPermission));
        when(permissionRepository.save(any(Permission.class))).thenReturn(updatedPermission);
        when(permissionMapper.toPermissionDto(updatedPermission)).thenReturn(updatedPermissionDto);

        //WHEN
        PermissionDto result = permissionService.updateDescription(permissionId, newDescription);

        //THEN
        assertThat(result).isEqualTo(updatedPermissionDto);

        verify(permissionRepository).findById(permissionId);
        verify(permissionRepository).save(any(Permission.class));
        verify(permissionMapper).toPermissionDto(updatedPermission);
    }

    @Test
    void updateDescription_whenPermissionNotFound_thenThrowsException() {
        //GIVEN
        final Long permissionId = 999L;
        final String newDescription = "Новое описание";

        when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

        //WHEN & THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> permissionService.updateDescription(permissionId, newDescription))
                .satisfies(ex -> {
                    assertThat(ex.getErrorMessageCode()).isEqualTo("messages.permission.not-found.id");
                    assertThat(ex.getArgs()).containsExactly(permissionId);
                });

        verify(permissionRepository).findById(permissionId);

        verifyNoMoreInteractions(permissionRepository);
        verifyNoInteractions(permissionMapper);
    }

    @Test
    void delete_whenNoRelatedEntities_thenDeletesRecord() {
        //GIVEN
        final Long permissionId = 2L;

        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testData.WRITE_PERMISSION));
        when(roleRepository.existsByPermissions(Collections.singleton(testData.WRITE_PERMISSION)))
                .thenReturn(false); // No related entities found

        //WHEN
        permissionService.delete(permissionId);

        //THEN
        verify(permissionRepository, times(1)).deleteById(permissionId);
    }

    @Test
    void delete_whenHasRelatedEntities_thenThrowsException() {
        //GIVEN
        final Long permissionId = 2L;

        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testData.WRITE_PERMISSION));
        when(roleRepository.existsByPermissions(Collections.singleton(testData.WRITE_PERMISSION)))
                .thenReturn(true); // Related entities found

        //WHEN & THEN
        assertThatExceptionOfType(RelatedEntityExistsException.class)
                .isThrownBy(() -> permissionService.delete(permissionId));

        verify(permissionRepository, never()).deleteById(permissionId);
    }

    private static class TestData {
        final Permission READ_PERMISSION = new Permission(1L, "user.read", "Право на чтение пользователей");
        final Permission WRITE_PERMISSION = new Permission(2L, "user.write", "Право на запись пользователей");
        final Permission DELETE_PERMISSION = new Permission(3L, "user.delete", "Право на удаление пользователей");

        final PermissionDto READ_PERMISSION_DTO = new PermissionDto(1L, "user.read", "Право на чтение пользователей");
        final PermissionDto WRITE_PERMISSION_DTO = new PermissionDto(2L, "user.write", "Право на запись пользователей");
        final PermissionDto DELETE_PERMISSION_DTO = new PermissionDto(3L, "user.delete", "Право на удаление пользователей");

        final Pageable NO_SORT_PAGINATION_REQUEST =
                new EntityPaginationRequest(0, 10, null, null).buildPageableFromRequest();
    }
}