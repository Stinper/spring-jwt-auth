package me.stinper.jwtauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
    @EqualsAndHashCode.Include
    private String roleName;

    @Column(name = "prefix", nullable = false, length = Constraints.PREFIX_FIELD_MAX_LENGTH)
    private String prefix;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "roles_permissions",
            joinColumns = @JoinColumn(name = "role_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "permission_id", nullable = false),
            uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission_id"})
    )
    @ToString.Exclude
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

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
