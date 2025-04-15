package me.stinper.jwtauth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;

@Entity
@Table(name = "permissions")
@Data
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
    private String permission;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Override
    public String getAuthority() {
        return this.permission;
    }
}
