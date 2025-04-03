package me.stinper.jwtauth.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordValidator implements ConstraintValidator<Password, String> {
    private final MessageSourceHelper messageSourceHelper;
    private final PasswordPolicyProperties passwordPolicyProperties;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        if (password == null) return true; //Должно быть обработано другой аннотацией

        boolean passwordValid = true;
        int lettersCount = 0, upperLettersCount = 0;
        StringBuilder errorMessage = new StringBuilder();

        if (password.length() < passwordPolicyProperties.getMinLength()) {
            errorMessage.append(messageSourceHelper.getLocalizedMessage(
                    "messages.user.validation.fields.password.min-length",
                    passwordPolicyProperties.getMinLength())
            ).append(" ");
            passwordValid = false;
        }

        for(int i = 0; i < password.length(); i++) {
            if(Character.isLetter(password.charAt(i)))
                lettersCount++;

            if(Character.isUpperCase(password.charAt(i)))
                upperLettersCount++;
        }

        if(lettersCount < passwordPolicyProperties.getMinLettersCount()) {
            errorMessage.append(messageSourceHelper.getLocalizedMessage(
                    "messages.user.validation.fields.password.min-letters-count",
                    passwordPolicyProperties.getMinLettersCount())
            ).append(" ");
            passwordValid = false;
        }

        if(upperLettersCount < passwordPolicyProperties.getMinUpperLettersCount()) {
            errorMessage.append(messageSourceHelper.getLocalizedMessage(
                    "messages.user.validation.fields.password.min-upper-letters-count",
                    passwordPolicyProperties.getMinUpperLettersCount())
            ).append(" ");
            passwordValid = false;
        }

        if(!passwordValid) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(errorMessage.toString())
                    .addConstraintViolation();

            return false;
        }

        return true;
    }
}
