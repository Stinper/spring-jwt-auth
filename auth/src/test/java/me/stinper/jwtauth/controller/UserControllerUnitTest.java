package me.stinper.jwtauth.controller;

import jakarta.validation.ConstraintViolationException;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.dto.EntityPaginationRequest;
import me.stinper.jwtauth.dto.user.PasswordChangeRequest;
import me.stinper.jwtauth.dto.user.UserCreationRequest;
import me.stinper.jwtauth.dto.user.UserDto;
import me.stinper.jwtauth.exception.EntityValidationException;
import me.stinper.jwtauth.exception.ResourceNotFoundException;
import me.stinper.jwtauth.service.entity.contract.UserPasswordService;
import me.stinper.jwtauth.service.entity.contract.UserService;
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
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for UserController class")
@ExtendWith(MockitoExtension.class)
class UserControllerUnitTest {
    @Mock private UserService userService;
    @Mock private UserPasswordService userPasswordService;
    @Mock private jakarta.validation.Validator validator;

    @InjectMocks
    private UserController userController;


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
        assertThatThrownBy(() -> userController.findAll(invalidPaginationRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(errorMessage);

        verifyNoInteractions(userService);
    }


    @Test
    void findAll_whenEntityPaginationRequestIsValid_thenReturnsPaginatedResult() {
        //GIVEN
        EntityPaginationRequest validEntityPaginationRequest = new EntityPaginationRequest(
                0, 10, null, null
        );

        Pageable pageable = validEntityPaginationRequest.buildPageableFromRequest();

        final UserDto firstUser = UserDto.builder()
                .uuid(UUID.fromString("9ee513f0-1cf3-4783-b822-6799406d9d81"))
                .email("firstuser@gmail.com")
                .build();

        final UserDto secondUser = UserDto.builder()
                .uuid(UUID.fromString("43ef0a73-09ec-43b2-8d34-9c8ec78ff24a"))
                .email("seconduser@gmail.com")
                .build();

        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(userService.findAll(pageable)).thenReturn(
                new PageImpl<>(
                        List.of(firstUser, secondUser),
                        pageable,
                        2
                )
        );

        //WHEN
        ResponseEntity<Page<UserDto>> result = userController.findAll(validEntityPaginationRequest);

        //THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull()
                .hasSize(2)
                .contains(firstUser, secondUser);

        verify(userService).findAll(pageable);
    }


    @Test
    void findByUUID_whenUserExists_thenReturnsUserDto() {
        //GIVEN
        final  UUID uuid = UUID.fromString("9ee513f0-1cf3-4783-b822-6799406d9d81");
        final UserDto user = UserDto.builder()
                .uuid(uuid)
                .email("firstuser@gmail.com")
                .build();

        when(userService.findByUUID(uuid)).thenReturn(Optional.of(user));

        //WHEN
        ResponseEntity<UserDto> result = userController.findById(uuid);

        //THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody())
                .isNotNull()
                .isEqualTo(user);

        verify(userService).findByUUID(uuid);
    }


    @Test
    void findByUUID_whenUserDoesNotExists_thenReturns404CodeWithEmptyBody() {
        //GIVEN
        when(userService.findByUUID(any())).thenReturn(Optional.empty());

        //WHEN
        ResponseEntity<UserDto> result = userController.findById(UUID.randomUUID());

        //THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
    }


    @Test
    void create_whenUserCreationRequestValidationFails_thenThrowsException() {
        //GIVEN
        UserCreationRequest invalidUserCreationRequest = new UserCreationRequest(
                "user@gmail.com", "123"
        );

        final String errorMessage = "Password is too short";

        ConstraintViolationMockSupport.mockValidatorToReturnSingleConstraintViolation(
                validator, errorMessage, invalidUserCreationRequest
        );

        //WHEN & THEN
        assertThatThrownBy(() -> userController.create(invalidUserCreationRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(errorMessage);

        verifyNoInteractions(userService);
    }


    @Test
    void create_whenUserCreationRequestIsValid_thenReturnsResult() {
        //GIVEN
        final UserDto user = UserDto.builder()
                .uuid(UUID.fromString("9ee513f0-1cf3-4783-b822-6799406d9d81"))
                .email("firstuser@gmail.com")
                .build();

        final UserCreationRequest userCreationRequest = new UserCreationRequest(
                "firstuser@gmail.com", "123"
        );

        final URI location = URI.create("http://localhost:8080/api/v1/jwt-auth/users/" + user.uuid());

        ServletUriComponentsBuilderMockSupport.withMockedUriComponentsBuilder(location, () -> {
            when(validator.validate(any())).thenReturn(Collections.emptySet());
            when(userService.create(userCreationRequest)).thenReturn(user);

            //WHEN
            ResponseEntity<?> createdUserResponse = userController.create(userCreationRequest);

            //THEN
            assertThat(createdUserResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(createdUserResponse.getBody()).isEqualTo(user);
            assertThat(createdUserResponse.getHeaders().getLocation()).isEqualTo(location);

            verify(userService).create(userCreationRequest);
        });
    }


    @Test
    void deleteByUUID_whenUserExists_thenReturnsNoContent() {
        //GIVEN
        final UUID uuid = UUID.fromString("9ee513f0-1cf3-4783-b822-6799406d9d81");
        doNothing().when(userService).deleteByUUID(uuid);

        //WHEN
        ResponseEntity<?> response = userController.deleteByUUID(uuid);

        //THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(userService).deleteByUUID(uuid);
    }


    @Test
    void deleteByUUID_whenUserDoesNotExist_thenThrowsException() {
        //GIVEN
        final UUID uuid = UUID.fromString("9ee513f0-1cf3-4783-b822-6799406d9d81");
        doThrow(ResourceNotFoundException.class).when(userService).deleteByUUID(uuid);

        //WHEN & THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> userController.deleteByUUID(uuid));
    }


    @Test
    void changePassword_whenChangePasswordRequestValidationFails_thenThrowsException() {
        //GIVEN
        final PasswordChangeRequest invalidPasswordChangeRequest = new PasswordChangeRequest(
                "123", "1234", "123"
        );

        final String errorMessage = "Passwords does not match";
        final JwtAuthUserDetails userDetails = mock(JwtAuthUserDetails.class);

        ConstraintViolationMockSupport.mockValidatorToReturnSingleConstraintViolation(
                validator, errorMessage, invalidPasswordChangeRequest);

        //WHEN & THEN
        assertThatThrownBy(() -> userController.changePassword(invalidPasswordChangeRequest, userDetails))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(errorMessage);

        verifyNoInteractions(userPasswordService);
    }


    @Test
    void changePassword_whenRequestValidationInServiceFails_thenThrowsException() {
        //GIVEN
        final PasswordChangeRequest invalidPasswordChangeRequest = new PasswordChangeRequest(
                "123", "1234", "123"
        );

        final JwtAuthUserDetails userDetails = mock(JwtAuthUserDetails.class);

        when(validator.validate(any())).thenReturn(Collections.emptySet()); //Java Bean Validation Successful
        doThrow(EntityValidationException.class).when(userPasswordService).changePassword(invalidPasswordChangeRequest, userDetails);

        //WHEN & THEN
        assertThatExceptionOfType(EntityValidationException.class)
                .isThrownBy(() -> userController.changePassword(invalidPasswordChangeRequest, userDetails));

        verify(userPasswordService).changePassword(invalidPasswordChangeRequest, userDetails);
    }


    @Test
    void changePassword_whenChangePasswordRequestIsValid_thenReturnsNoContentResponse() {
        //GIVEN
        final PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest(
                "123", "1234", "1234"
        );

        final JwtAuthUserDetails userDetails = mock(JwtAuthUserDetails.class);

        when(validator.validate(any())).thenReturn(Collections.emptySet());
        doNothing().when(userPasswordService).changePassword(passwordChangeRequest, userDetails);

        //WHEN
        ResponseEntity<?> response = userController.changePassword(passwordChangeRequest, userDetails);

        //THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(userPasswordService).changePassword(passwordChangeRequest, userDetails);
    }
}