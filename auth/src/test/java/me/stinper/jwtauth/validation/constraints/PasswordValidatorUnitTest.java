package me.stinper.jwtauth.validation.constraints;

import jakarta.validation.ConstraintValidatorContext;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test for PasswordValidator class")
class PasswordValidatorUnitTest {
    @Mock private MessageSourceHelper messageSourceHelper;
    @Mock private PasswordPolicyProperties passwordPolicyProperties;

    @InjectMocks
    private PasswordValidator passwordValidator;

    @Test
    void isValid_whenPasswordIsNull_thenReturnsTrue() {
        //WHEN
        boolean isPasswordValid = passwordValidator.isValid(null, null);

        //THEN
        assertThat(isPasswordValid).isTrue();
    }


    @Test
    void isValid_whenPasswordMatchesPolicyProperties_thenReturnsTrue() {
        //GIVEN
        final String password = "123abcABC";

        when(passwordPolicyProperties.getMinLength()).thenReturn(3);
        when(passwordPolicyProperties.getMinLettersCount()).thenReturn(3);
        when(passwordPolicyProperties.getMinUpperLettersCount()).thenReturn(3);

        //WHEN
        boolean isPasswordValid = passwordValidator.isValid(password, null);

        //THEN
        assertThat(isPasswordValid).isTrue();
        verify(passwordPolicyProperties, atLeastOnce()).getMinLength();
        verify(passwordPolicyProperties, atLeastOnce()).getMinLettersCount();
        verify(passwordPolicyProperties, atLeastOnce()).getMinUpperLettersCount();
    }


    @Test
    void isValid_whenPasswordLengthLessThanMinLength_thenReturnsFalse() {
        //GIVEN
        final String password = "123";

        //WHEN
        boolean isPasswordValid = this.testPasswordMatchesPolicyProperties(
                password, password.length() + 1, 0, 0);

        //THEN
        assertThat(isPasswordValid).isFalse();
        verify(passwordPolicyProperties, atLeastOnce()).getMinLength();
        verify(messageSourceHelper).getLocalizedMessage(any(), any());
    }


    @Test
    void isValid_whenPasswordLettersCountLessThanMinLettersCount_thenReturnsFalse() {
        //GIVEN
        final String password = "abc";

        //WHEN
        boolean isPasswordValid = this.testPasswordMatchesPolicyProperties(
                password, 0, password.length() + 1, 0);

        //THEN
        assertThat(isPasswordValid).isFalse();
        verify(passwordPolicyProperties, atLeastOnce()).getMinLettersCount();
        verify(messageSourceHelper).getLocalizedMessage(any(), any());
    }


    @Test
    void isValid_whenPasswordUpperLettersCountLessThenMinUpperLettersCount_thenReturnsFalse() {
        //GIVEN
        final String password = "ABC";

        //WHEN
        boolean isPasswordValid = this.testPasswordMatchesPolicyProperties(
                password, 0, 0, password.length() + 1);

        //THEN
        assertThat(isPasswordValid).isFalse();
        verify(passwordPolicyProperties, atLeastOnce()).getMinUpperLettersCount();
        verify(messageSourceHelper).getLocalizedMessage(any(), any());
    }


    private boolean testPasswordMatchesPolicyProperties(String password,
                                                        int minLength,
                                                        int minLettersCount,
                                                        int minUpperLettersCount) {
        //GIVEN
        when(passwordPolicyProperties.getMinLength()).thenReturn(minLength);
        when(passwordPolicyProperties.getMinLettersCount()).thenReturn(minLettersCount);
        when(passwordPolicyProperties.getMinUpperLettersCount()).thenReturn(minUpperLettersCount);

        when(messageSourceHelper.getLocalizedMessage(any(), any())).thenReturn("ERROR_MESSAGE");

        ConstraintValidatorContext contextMock = mock(ConstraintValidatorContext.class);
        ConstraintViolationBuilder violationBuilderMock = mock(ConstraintViolationBuilder.class);

        when(contextMock.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(violationBuilderMock);

        when(violationBuilderMock.addConstraintViolation())
                .thenReturn(contextMock);

        //WHEN
        boolean isPasswordValid = passwordValidator.isValid(password, contextMock);

        //THEN
        verify(contextMock).disableDefaultConstraintViolation();
        verify(contextMock).buildConstraintViolationWithTemplate(anyString());
        verify(violationBuilderMock).addConstraintViolation();

        return isPasswordValid;
    }
}