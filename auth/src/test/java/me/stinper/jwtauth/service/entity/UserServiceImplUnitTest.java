package me.stinper.jwtauth.service.entity;

import me.stinper.jwtauth.dto.EntityPaginationRequest;
import me.stinper.jwtauth.dto.role.RoleDto;
import me.stinper.jwtauth.dto.user.UserCreationRequest;
import me.stinper.jwtauth.dto.user.UserDto;
import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.exception.EntityValidationException;
import me.stinper.jwtauth.exception.NoSuchPropertyException;
import me.stinper.jwtauth.exception.ResourceNotFoundException;
import me.stinper.jwtauth.mapping.UserMapper;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.service.authentication.contract.JwtService;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import me.stinper.jwtauth.validation.UserCreationValidator;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.SimpleErrors;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for UserServiceImpl class")
class UserServiceImplUnitTest {
    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private UserCreationValidator userCreationValidator;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MessageSourceHelper messageSourceHelper;
    @Mock private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl userService;

    private TestData testData;

    @BeforeEach
    void setUp() {
        this.testData = new TestData();
    }

    @Test
    @DisplayName(
            """
            [#findAll]: Checks that the method returns a valid page of records of type
             UserDto retrieved from the repository as User objects
            """
    )
    void findAll_returnsCorrectRecordsPage() {
        //GIVEN
        when(userMapper.toUserDto(testData.SIMPLE_USER)).thenReturn(testData.SIMPLE_USER_DTO);
        when(userMapper.toUserDto(testData.USER_WITH_ROLES)).thenReturn(testData.USER_WITH_ROLES_DTO);

        Pageable pageable = testData.NO_SORT_PAGINATION_REQUEST;

        when(userRepository.findAllByDeactivatedAtIsNull(pageable)).thenReturn(
                new PageImpl<>(
                        List.of(testData.SIMPLE_USER, testData.USER_WITH_ROLES),
                        pageable,
                        2
                )
        );

        //WHEN
        Page<UserDto> users = userService.findAll(pageable);

        //THEN
        assertThat(users)
                .hasSize(2)
                .containsAll(List.of(testData.SIMPLE_USER_DTO, testData.USER_WITH_ROLES_DTO))
                .doesNotContain(testData.DEACTIVATED_USER_DTO);

        verify(userRepository, times(1)).findAllByDeactivatedAtIsNull(pageable);
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
        when(userRepository.findAllByDeactivatedAtIsNull(any()))
                .thenThrow(
                        new PropertyReferenceException("nonExistentProperty", TypeInformation.OBJECT, Collections.emptyList())
                );

        //WHEN & THEN
        assertThatExceptionOfType(NoSuchPropertyException.class)
                .isThrownBy(() -> userService.findAll(Pageable.unpaged()))
                .satisfies(ex -> assertThat(
                        ex.getPropertyReferenceException().getPropertyName()).isEqualTo("nonExistentProperty")
                );
    }


    @Test
    @DisplayName(
            """
            [#findByUUID]: Checks that the method returns an object of type [UserDto] when it is passed a correct UUID
            """
    )
    void findByUUID_whenCorrectUUID_thenReturnsUser() {
        //GIVEN
        final UUID correctUUID = testData.SIMPLE_USER.getUuid();

        when(userMapper.toUserDto(testData.SIMPLE_USER)).thenReturn(testData.SIMPLE_USER_DTO);
        when(userRepository.findById(correctUUID)).thenReturn(Optional.of(testData.SIMPLE_USER));

        //WHEN
        Optional<UserDto> user = userService.findByUUID(correctUUID);

        //THEN
        assertThat(user).hasValue(testData.SIMPLE_USER_DTO);

        verify(userRepository, times(1)).findById(correctUUID);
    }


    @Test
    @DisplayName(
            """
            [#findByUUID]: Checks that the method returns an empty [Optional] object when it is passed a incorrect UUID
            """
    )
    void findByUUID_whenIncorrectUUID_thenReturnsEmptyOptional() {
        //GIVEN
        final UUID incorrectUUID = UUID.randomUUID();

        when(userRepository.findById(incorrectUUID)).thenReturn(Optional.empty());

        //WHEN
        Optional<UserDto> user = userService.findByUUID(incorrectUUID);

        //THEN
        assertThat(user).isEmpty();

        verify(userRepository, times(1)).findById(incorrectUUID);
    }


    @Test
    @DisplayName("[#create]: Checks that the method creates a new record when the request to create a record is valid")
    void create_whenCreationRequestIsValid_thenCreatesRecord() {
        //GIVEN
        final String
                email = testData.SIMPLE_USER.getEmail(),            //firstuser@gmail.com
                password = testData.SIMPLE_USER.getPassword();      //123

        UserCreationRequest userCreationRequest = new UserCreationRequest(email, password);
        User userWithoutDefaultFields = User.builder()
                .email(email)
                .password(password)
                .build();

        User createdUser = testData.SIMPLE_USER;

        when(userMapper.toUser(userCreationRequest)).thenReturn(userWithoutDefaultFields);
        when(userMapper.toUserDto(createdUser)).thenReturn(testData.SIMPLE_USER_DTO);
        when(passwordEncoder.encode(password)).thenReturn("$2y$10$znR5IKy4b4MKuHUbjPnPjePvj9u6ZOyCGUDVSyb8JOT5pMrwQaTQi");
        when(userRepository.save(userWithoutDefaultFields)).thenReturn(createdUser);

        //Empty Errors Object (Request is valid)
        when(userCreationValidator.validateObject(userCreationRequest)).thenReturn(new SimpleErrors(userCreationRequest));

        //WHEN
        UserDto user = userService.create(userCreationRequest);

        //THEN
        assertThat(user).isEqualTo(testData.SIMPLE_USER_DTO);

        verify(userRepository, times(1)).save(userWithoutDefaultFields);
    }


    @Test
    @DisplayName("[#create]: Checks that the method throws when the request to create a record is NOT valid")
    void create_whenCreationRequestIsInvalid_thenThrowsException() {
        //GIVEN
        UserCreationRequest invalidUserCreationRequest = new UserCreationRequest("invalid_email@gmail.com", "123");

        SimpleErrors userCreationErrors = new SimpleErrors(invalidUserCreationRequest);
        userCreationErrors.rejectValue("email", "ERROR_CODE");

        when(userCreationValidator.validateObject(invalidUserCreationRequest)).thenReturn(userCreationErrors);

        //WHEN & THEN
        Assertions.assertThrows(EntityValidationException.class, () -> userService.create(invalidUserCreationRequest));

        verify(userRepository, never()).save(any());
    }


    @Test
    @DisplayName(
            """
            [#deleteByUUID]: Checks that the method deactivates the user's account if the correct UUID has been passed to it
            """
    )
    void deleteByUUID_whenCorrectUUID_thenDeactivatesUser() {
        //GIVEN
        final UUID correctUUID = testData.SIMPLE_USER.getUuid();

        when(userRepository.findById(correctUUID)).thenReturn(Optional.of(testData.SIMPLE_USER));

        //WHEN
        userService.deleteByUUID(correctUUID);

        //THEN
        verify(userRepository, times(1)).findById(correctUUID);
        verify(userRepository).save(argThat(user -> user.getDeactivatedAt() != null));
        verify(jwtService, times(1)).invalidateRefreshTokens(testData.SIMPLE_USER);
    }


    @Test
    @DisplayName(
            """
            [#deleteByUUID]: Checks that the method throws exception if the incorrect UUID has been passed to it
            """
    )
    void deleteByUUID_whenIncorrectUUID_thenThrowsException() {
        //GIVEN
        final UUID incorrectUUID = UUID.randomUUID();

        when(userRepository.findById(incorrectUUID)).thenReturn(Optional.empty());

        //WHEN & THEN
        Assertions.assertThrows(ResourceNotFoundException.class, () -> userService.deleteByUUID(incorrectUUID));

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).invalidateRefreshTokens(any());
    }

    private static class TestData {
        final User SIMPLE_USER = User.builder()
                .uuid(UUID.fromString("d5b16719-3d4e-478e-90d5-0f6489adcb92"))
                .email("firstuser@gmail.com")
                .password("123")
                .build();
        final User DEACTIVATED_USER = User.builder()
                .uuid(UUID.fromString("77ff4ce0-2b35-48a7-a713-8053e0f0ae5e"))
                .email("seconduser@gmail.com")
                .deactivatedAt(Instant.now())
                .password("123")
                .build();
        final User USER_WITH_ROLES = User.builder()
                .uuid(UUID.fromString("c167f5dc-e404-415e-9a5e-73bf5de1b963"))
                .email("thirduser@gmail.com")
                .password("123")
                .roles(
                        List.of(
                                new Role(1L, "ROLE_ADMIN")
                        )
                )
                .build();

        final UserDto SIMPLE_USER_DTO = toDto(SIMPLE_USER);
        final UserDto DEACTIVATED_USER_DTO = toDto(DEACTIVATED_USER);
        final UserDto USER_WITH_ROLES_DTO = toDto(USER_WITH_ROLES);

        final Pageable NO_SORT_PAGINATION_REQUEST =
                new EntityPaginationRequest(0, 10, null, null).buildPageableFromRequest();

        private static UserDto toDto(User user) {
            return UserDto.builder()
                    .uuid(user.getUuid())
                    .email(user.getEmail())
                    .registeredAt(user.getRegisteredAt())
                    .isEmailVerified(user.getIsEmailVerified())
                    .roles(
                            Optional.ofNullable(user.getRoles())
                                    .map(roles ->
                                            roles.stream()
                                                    .map(role -> new RoleDto(role.getId(), role.getRoleName()))
                                                    .toList()
                                    )
                                    .orElse(Collections.emptyList()))
                    .build();
        }
    }
}
