package me.stinper.jwtauth.service.authentication;

import me.stinper.jwtauth.dto.JwtResponse;
import me.stinper.jwtauth.dto.user.LoginRequest;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.exception.ResourceNotFoundException;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.service.authentication.contract.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test for AuthServiceImpl class")
class AuthServiceImplUnitTest {
    @Mock private UserRepository userRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private TestData testData;

    @BeforeEach
    void setUp() {
        this.testData = new TestData();
    }


    @Test
    void login_whenUserCredentialsIsCorrect_thenReturnsTokensPair() {
        //GIVEN
        when(authenticationManager.authenticate(any())).thenReturn(null); //Do not throw exception -> authentication successful
        when(userRepository.findByEmailIgnoreCase(testData.LOGIN_REQUEST.email())).thenReturn(Optional.of(testData.SIMPLE_USER));
        when(jwtService.generateTokensPair(testData.SIMPLE_USER)).thenReturn(testData.JWT_RESPONSE);

        //WHEN
        JwtResponse tokensPair = authService.login(testData.LOGIN_REQUEST);

        //THEN
        assertThat(tokensPair).isEqualTo(testData.JWT_RESPONSE);

        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, times(1)).findByEmailIgnoreCase(testData.LOGIN_REQUEST.email());
        verify(jwtService, times(1)).generateTokensPair(testData.SIMPLE_USER);
    }


    @Test
    void login_whenUserDoesNotExists_thenThrowsException() {
        //GIVEN
        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());

        //WHEN & THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> authService.login(testData.LOGIN_REQUEST))
                .satisfies(ex -> {
                    assertThat(ex.getErrorMessageCode()).isEqualTo("messages.user.not-found.email");
                    assertThat(ex.getArgs()).containsExactly(testData.LOGIN_REQUEST.email());
                });

        verify(userRepository).findByEmailIgnoreCase(testData.LOGIN_REQUEST.email());
        verify(jwtService, never()).generateTokensPair(any());
    }


    @ParameterizedTest
    @MethodSource("authenticationExceptionsSource")
    void login_whenAuthenticationFails_thenThrowsException(Class<? extends AuthenticationException> authenticationExceptionClass) {
        //GIVEN
        when(authenticationManager.authenticate(any())).thenThrow(authenticationExceptionClass);

        //WHEN & THEN
        assertThatExceptionOfType(authenticationExceptionClass)
                .isThrownBy(() -> authService.login(testData.LOGIN_REQUEST));

        verify(jwtService, never()).generateTokensPair(any());
    }


    @Test
    void logout_whenMethodCalls_thenInvalidateRefreshTokens() {
        //WHEN
        authService.logout(testData.SIMPLE_USER);

        //THEN
        verify(jwtService, times(1)).invalidateRefreshTokens(testData.SIMPLE_USER);
    }


    private static Stream<Arguments> authenticationExceptionsSource() {
        return Stream.of(
                Arguments.of(BadCredentialsException.class),
                Arguments.of(LockedException.class),
                Arguments.of(DisabledException.class)
        );
    }


    private static class TestData {
        final User SIMPLE_USER = User.builder()
                .uuid(UUID.fromString("fe6bfb16-e3f9-40e5-acf1-5d60f1216f08"))
                .email("user@gmail.com")
                .password("123")
                .build();

        final LoginRequest LOGIN_REQUEST = new LoginRequest(
                "user@gmail.com", "123"
        );


        final JwtResponse JWT_RESPONSE = new JwtResponse(
                "ACCESS_TOKEN",
                "REFRESH_TOKEN"
        );

    }
}
