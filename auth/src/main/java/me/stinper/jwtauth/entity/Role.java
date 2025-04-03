package me.stinper.jwtauth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true, length = Constraints.ROLE_NAME_FIELD_MAX_LENGTH)
    private String roleName;

    @Override
    public String getAuthority() {
        return this.roleName;
    }

    /**
     * Интерфейс, содержащий в виде констант времени компиляции возможную длину каждого из строковых полей.
     * Эти константы используются в валидации (Java Bean Validation), чтобы не хардкодить максимальную длину поля
     */
    public interface Constraints {
        int ROLE_NAME_FIELD_MAX_LENGTH = 255;
    }
}
