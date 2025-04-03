package me.stinper.jwtauth.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.validation.constraints.Password;

@Builder
public record UserCreationRequest(
        @NotBlank(message = "{messages.user.validation.fields.email.blank}")
        @Email(
                message = "{messages.user.validation.fields.email.incorrect-pattern}",
                regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        @Size(
                max = User.Constraints.EMAIL_FIELD_MAX_LENGTH,
                message = "{messages.user.validation.fields.email.too-long}"
        )
        String email,

        @NotBlank(message = "{messages.user.validation.fields.password.blank}")
        @Password
        String password
) {}
