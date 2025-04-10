package me.stinper.jwtauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role implements GrantedAuthority {
    @Serial
    private static final long serialVersionUID = 9043550632440554112L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true, length = Constraints.ROLE_NAME_FIELD_MAX_LENGTH)
    private String roleName;

    @Column(name = "prefix", nullable = false, length = Constraints.PREFIX_FIELD_MAX_LENGTH)
    private String prefix;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "roles_permissions",
            joinColumns = @JoinColumn(name = "role_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "permission_id", nullable = false)
    )
    @ToString.Exclude
    @Builder.Default
    private List<Permission> permissions = new ArrayList<>();

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
        int PREFIX_FIELD_MAX_LENGTH = 255;
    }
}
