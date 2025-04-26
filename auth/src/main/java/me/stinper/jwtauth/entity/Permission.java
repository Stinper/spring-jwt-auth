package me.stinper.jwtauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Permission implements GrantedAuthority {
    @Serial
    private static final long serialVersionUID = -523984582402502857L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String permission;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Override
    public String getAuthority() {
        return this.permission;
    }
}
