package me.stinper.jwtauth.validation.constraints;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class PasswordPolicyProperties {
    @Value("${app.auth.security.password.min.length}")
    private int minLength;

    @Value("${app.auth.security.password.min.letters-count}")
    private int minLettersCount;

    @Value("${app.auth.security.password.min.upper-letters-count}")
    private int minUpperLettersCount;
}
