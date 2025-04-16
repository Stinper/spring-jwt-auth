package me.stinper.jwtauth.entity;

import jakarta.persistence.*;
import lombok.*;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements JwtAuthUserDetails {
    @Serial
    private static final long serialVersionUID = -2442103282310823899L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(name = "email", nullable = false, unique = true, length = Constraints.EMAIL_FIELD_MAX_LENGTH)
    private String email;

    @Column(name = "password", nullable = false)
    @ToString.Exclude
    private String password;

    @Column(name = "registered_at", nullable = false, insertable = false, updatable = false)
    @Builder.Default
    private Instant registeredAt = Instant.now();

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(name = "deactivated_at")
    private Instant deactivatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "role_id", nullable = false),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"})
    )
    @ToString.Exclude
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<RefreshToken> refreshTokens = new ArrayList<>();


    @PrePersist
    public void initializeDefaultFields() {
        if (this.registeredAt == null)
            this.registeredAt = Instant.now();

        if (this.isEmailVerified == null)
            this.isEmailVerified = false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Role role : this.roles) {
            authorities.add(role);
            authorities.addAll(role.getPermissions());
        }

        return authorities;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isEnabled() {
        return this.deactivatedAt == null;
    }

    /**
     * Интерфейс, содержащий в виде констант времени компиляции возможную длину каждого из строковых полей.
     * Эти константы используются в валидации (Java Bean Validation), чтобы не хардкодить максимальную длину поля
     */
    public interface Constraints {
        int EMAIL_FIELD_MAX_LENGTH = 255;
    }
}
