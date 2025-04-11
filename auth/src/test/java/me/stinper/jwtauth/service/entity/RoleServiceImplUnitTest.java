package me.stinper.jwtauth.service.entity;

import me.stinper.jwtauth.dto.EntityPaginationRequest;
import me.stinper.jwtauth.dto.permission.PermissionDto;
import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.dto.role.RoleDto;
import me.stinper.jwtauth.dto.role.RolePermissionUpdateRequest;
import me.stinper.jwtauth.entity.Permission;
import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.exception.EntityValidationException;
import me.stinper.jwtauth.exception.NoSuchPropertyException;
import me.stinper.jwtauth.exception.RelatedEntityExistsException;
import me.stinper.jwtauth.exception.ResourceNotFoundException;
import me.stinper.jwtauth.mapping.RoleMapper;
import me.stinper.jwtauth.repository.PermissionRepository;
import me.stinper.jwtauth.repository.RoleRepository;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.validation.RoleCreationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for RoleServiceImpl class")
class RoleServiceImplUnitTest {
    @Mock private RoleRepository roleRepository;
    @Mock private UserRepository userRepository;
    @Mock private PermissionRepository permissionRepository;
    @Mock private RoleMapper roleMapper;
    @Mock private RoleCreationValidator roleCreationValidator;

    @InjectMocks
    private RoleServiceImpl roleService;

    private TestData testData;


    @BeforeEach
    void setUp() {
        this.testData = new TestData();
    }

    @Test
    @DisplayName(
            """
            [#findAll]: Checks that the method returns a valid page of records of type
             RoleDto retrieved from the repository as Role objects
            """
    )
    void findAll_returnsCorrectRecordsPage() {
        //GIVEN
        when(roleMapper.toRoleDto(testData.ADMIN_ROLE)).thenReturn(testData.ADMIN_ROLE_DTO);
        when(roleMapper.toRoleDto(testData.MANAGER_ROLE)).thenReturn(testData.MANAGER_ROLE_DTO);
        when(roleMapper.toRoleDto(testData.USER_ROLE)).thenReturn(testData.USER_ROLE_DTO);

        when(roleRepository.findAll(testData.NO_SORT_PAGINATION_REQUEST)).thenReturn(
                new PageImpl<>(
                        List.of(testData.ADMIN_ROLE, testData.MANAGER_ROLE, testData.USER_ROLE),
                        testData.NO_SORT_PAGINATION_REQUEST,
                        3
                )
        );

        //WHEN
        Page<RoleDto> roles = roleService.findAll(testData.NO_SORT_PAGINATION_REQUEST);

        //THEN
        assertThat(roles).hasSize(3).containsAll(
                List.of(testData.ADMIN_ROLE_DTO, testData.MANAGER_ROLE_DTO, testData.USER_ROLE_DTO)
        );

        verify(roleRepository, times(1)).findAll(testData.NO_SORT_PAGINATION_REQUEST);
    }


    @Test
    @DisplayName(
            """
            [#findAll]: Checks that the method throws an exception if an attempt is made
             to sort the resulting set of values by a non-existent field.
            """
    )
    void findAll_whenSortByNotExistentProperty_thenThrowsException() {
        //GIVEN
        when(roleRepository.findAll(any(Pageable.class)))
                .thenThrow(
                        new PropertyReferenceException("nonExistentProperty", TypeInformation.OBJECT, Collections.emptyList())
                );

        //WHEN & THEN
        assertThatExceptionOfType(NoSuchPropertyException.class)
                .isThrownBy(() -> roleService.findAll(Pageable.unpaged()))
                .satisfies(ex -> assertThat(
                        ex.getPropertyReferenceException().getPropertyName()).isEqualTo("nonExistentProperty")
                );
    }


    @Test
    @DisplayName(
            """
            [#findByName]: Checks that the method returns an object of type [RoleDto] when it is passed a correct role name
            """
    )
    void findByName_whenCorrectName_thenReturnsRole() {
        //GIVEN
        final String roleName = "ROLE_ADMIN";

        when(roleMapper.toRoleDto(testData.ADMIN_ROLE)).thenReturn(testData.ADMIN_ROLE_DTO);
        when(roleRepository.findByRoleNameIgnoreCase(roleName)).thenReturn(Optional.of(testData.ADMIN_ROLE));

        //WHEN
        Optional<RoleDto> roleDto = roleService.findByName(roleName);

        //THEN
        assertThat(roleDto).hasValue(testData.ADMIN_ROLE_DTO);

        verify(roleRepository, times(1)).findByRoleNameIgnoreCase(roleName);
    }


    @Test
    @DisplayName(
            """
            [#findByName]: Checks that the method returns an empty [Optional] object when it is passed a incorrect role name
            """
    )
    void findByName_whenIncorrectName_thenReturnsEmptyOptional() {
        //GIVEN
        final String roleName = "INVALID_ROLE_NAME";

        when(roleRepository.findByRoleNameIgnoreCase(roleName)).thenReturn(Optional.empty());

        //WHEN
        Optional<RoleDto> roleDto = roleService.findByName(roleName);

        //THEN
        assertThat(roleDto).isEmpty();

        verify(roleRepository, times(1)).findByRoleNameIgnoreCase(roleName);
    }


    @Test
    @DisplayName(
            """
            [#create]: Checks that the method creates a new record when the request to create a record is valid
            """
    )
    void create_whenCreationRequestIsValid_thenCreatesRecord() {
        //GIVEN
        final String roleName = testData.ADMIN_ROLE.getRoleName();

        RoleCreationRequest roleCreationRequest = new RoleCreationRequest(
                roleName, "Администратор", Set.of("some.admin.permission", "some.another.admin.permission")
        );

        when(roleMapper.toRole(roleCreationRequest)).thenReturn(Role.builder().build());
        when(roleMapper.toRoleDto(testData.ADMIN_ROLE)).thenReturn(testData.ADMIN_ROLE_DTO);

        when(roleRepository.save(any())).thenReturn(testData.ADMIN_ROLE);
        when(roleCreationValidator.validateObject(roleCreationRequest)).thenReturn(new SimpleErrors(roleCreationRequest));

        //WHEN
        RoleDto roleDto = roleService.create(roleCreationRequest);

        //THEN
        assertThat(roleDto).isEqualTo(testData.ADMIN_ROLE_DTO);

        verify(roleRepository).save(any());
    }


    @Test
    void updatePermissions_whenRoleNotFound_thenThrowsException() {
        //GIVEN
        final String roleName = "ROLE_USER";
        when(roleRepository.findByRoleNameIgnoreCase(roleName)).thenReturn(Optional.empty());

        //WHEN & THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> roleService.updatePermissions(roleName, mock(RolePermissionUpdateRequest.class)))
                .satisfies(ex -> {
                    assertThat(ex.getErrorMessageCode()).isEqualTo("messages.role.not-found.role-name");
                    assertThat(ex.getArgs()).containsExactly(roleName);
                });

        verify(roleRepository).findByRoleNameIgnoreCase(roleName);

        verifyNoMoreInteractions(roleRepository);
        verifyNoInteractions(permissionRepository, roleCreationValidator, roleMapper);
    }


    @Test
    void updatePermissions_whenRolePermissionsUpdateRequestValidationFails_thenThrowsException() {
        //GIVEN
        final String roleName = "ROLE_ADMIN";

        final RolePermissionUpdateRequest request = new RolePermissionUpdateRequest(
                Set.of("some.incorrect.permission")
        );

        when(roleRepository.findByRoleNameIgnoreCase(roleName)).thenReturn(Optional.of(testData.ADMIN_ROLE));

        doAnswer(ans -> {
            Errors errors = ans.getArgument(1, Errors.class);
            errors.rejectValue("permissions", "error.code", "Error Message");

            return null;
        }).when(roleCreationValidator).validateInputPermissions(eq(request.permissions()), any());

        //WHEN & THEN
        assertThatExceptionOfType(EntityValidationException.class)
                .isThrownBy(() -> roleService.updatePermissions(roleName, request))
                .satisfies(ex -> assertThat(ex.getFieldErrors())
                        .hasSize(1)
                        .first()
                        .satisfies(error -> {
                            assertThat(error.getField()).isEqualTo("permissions");
                            assertThat(error.getDefaultMessage()).isEqualTo("Error Message");
                            assertThat(error.getCode()).isEqualTo("error.code");
                        }));

        verify(roleRepository).findByRoleNameIgnoreCase(roleName);
        verify(roleCreationValidator).validateInputPermissions(eq(request.permissions()), any());

        verifyNoMoreInteractions(roleRepository);
    }


    @Test
    void updatePermissions_whenRolePermissionsUpdateRequestIsValid_thenUpdatesRolePermissions() {
        //GIVEN
        final String roleName = "ROLE_ADMIN";

        final RolePermissionUpdateRequest request = new RolePermissionUpdateRequest(
                Set.of("some.permission")
        );

        final Role existingRole = new Role(1L, roleName, "Администратор", Collections.emptyList());
        final Role updatedRole = new Role(1L, roleName, "Администратор",
                List.of(
                        new Permission(1L, "some.permission", null)
                )
        );

        final RoleDto updatedRoleDto = new RoleDto(1L, roleName, "Администратор",
                List.of(
                        new PermissionDto(1L, "some.permission", null)
                )
        );

        when(roleRepository.findByRoleNameIgnoreCase(roleName)).thenReturn(Optional.of(existingRole));
        when(permissionRepository.findAllByPermissionIn(request.permissions()))
                .thenReturn(
                        List.of(
                                new Permission(10L, "some.permission", null)
                        )
                );
        when(roleRepository.save(existingRole)).thenReturn(updatedRole);
        when(roleMapper.toRoleDto(updatedRole)).thenReturn(updatedRoleDto);
        doNothing().when(roleCreationValidator).validateInputPermissions(eq(request.permissions()), any(Errors.class));


        //WHEN
        RoleDto result = roleService.updatePermissions(roleName, request);

        //THEN
        assertThat(result).isEqualTo(updatedRoleDto);
        verify(roleRepository).findByRoleNameIgnoreCase(roleName);
        verify(roleCreationValidator).validateInputPermissions(eq(request.permissions()), any(Errors.class));
        verify(permissionRepository).findAllByPermissionIn(request.permissions());
        verify(roleRepository).save(existingRole);
    }


    @Test
    @DisplayName(
            """
            [#create]: Checks that the method throws when the request to create a record is NOT valid
            """
    )
    void create_whenRoleCreationRequestNotValid_thenThrowsException() {
        //GIVEN
        RoleCreationRequest invalidRoleCreationRequest = new RoleCreationRequest(
                "INVALID", "Prefix", Collections.emptySet()
        );

        Errors roleCreationRequestErrors = new SimpleErrors(invalidRoleCreationRequest);
        roleCreationRequestErrors.rejectValue("roleName", "code");

        when(roleCreationValidator.validateObject(invalidRoleCreationRequest)).thenReturn(roleCreationRequestErrors);

        //WHEN & THEN
        assertThatExceptionOfType(EntityValidationException.class)
                .isThrownBy(() -> roleService.create(invalidRoleCreationRequest));

        verify(roleRepository, never()).save(any());
    }


    @Test
    @DisplayName(
            """
            [#delete]: Checks that the method deletes the specified role if no related entities were found for it
            """
    )
    void delete_whenNoRelatedEntities_thenDeletesRecord() {
        //GIVEN
        final String roleName = testData.MANAGER_ROLE.getRoleName();

        when(roleRepository.findByRoleNameIgnoreCase(roleName)).thenReturn(Optional.of(testData.MANAGER_ROLE));
        when(userRepository.existsByRoles(Collections.singletonList(testData.MANAGER_ROLE)))
                .thenReturn(false); // No related entities found

        //WHEN
        roleService.delete(roleName);

        //THEN
        verify(roleRepository, times(1)).deleteByRoleNameIgnoreCase(roleName);
    }


    @Test
    @DisplayName(
            """
            [#delete]: Checks that the method throws exception if there was at least one related entity found for target entity
            """
    )
    void delete_whenHasRelatedEntities_thenThrowsException() {
        //GIVEN
        final String roleName = testData.MANAGER_ROLE.getRoleName();

        when(roleRepository.findByRoleNameIgnoreCase(roleName)).thenReturn(Optional.of(testData.MANAGER_ROLE));
        when(userRepository.existsByRoles(Collections.singletonList(testData.MANAGER_ROLE)))
                .thenReturn(true); // Related entities found

        //WHEN & THEN
        assertThatExceptionOfType(RelatedEntityExistsException.class)
                .isThrownBy(() -> roleService.delete(roleName));

        verify(roleRepository, never()).deleteByRoleNameIgnoreCase(roleName);
    }

    private static class TestData {
        final Role ADMIN_ROLE = new Role(1L, "ROLE_ADMIN", "Администратор", Collections.emptyList());
        final Role MANAGER_ROLE = new Role(2L, "ROLE_MANAGER", "Менеджер", Collections.emptyList());
        final Role USER_ROLE = new Role(3L, "ROLE_USER", "Пользователь", Collections.emptyList());

        final RoleDto ADMIN_ROLE_DTO = new RoleDto(1L, "ROLE_ADMIN", "Администратор", Collections.emptyList());
        final RoleDto MANAGER_ROLE_DTO = new RoleDto(2L, "ROLE_MANAGER", "Менеджер", Collections.emptyList());
        final RoleDto USER_ROLE_DTO = new RoleDto(3L, "ROLE_USER", "Пользователь", Collections.emptyList());

        final Pageable NO_SORT_PAGINATION_REQUEST =
                new EntityPaginationRequest(0, 10, null, null).buildPageableFromRequest();
    }
}
