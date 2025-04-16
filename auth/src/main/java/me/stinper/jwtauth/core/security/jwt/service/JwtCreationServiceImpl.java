package me.stinper.jwtauth.core.security.jwt.service;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.core.security.jwt.JwtSignatureKeysProvider;
import me.stinper.jwtauth.entity.Permission;
import me.stinper.jwtauth.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JwtCreationServiceImpl implements JwtCreationService {
    private final JwtSignatureKeysProvider signatureKeysProvider;

    @Value("${app.auth.security.jwt.access-token-expiration}")
    private Duration accessTokenExpiration;

    @Value("${app.auth.security.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Override
    public String createAccessToken(@NonNull JwtAuthUserDetails userDetails) {
        Collection<? extends GrantedAuthority> grantedAuthorities = userDetails.getAuthorities();

        Map<String, Object> authorities = extractAuthorities(grantedAuthorities);

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(userDetails.getUuid().toString())
                .expiration(getTokenExpirationAsDate(this.accessTokenExpiration))
                .claim("type", "ACCESS")
                .claim("authorities", authorities)
                .claim("email", userDetails.getUsername())
                .signWith(signatureKeysProvider.privateKey())
                .compact();
    }

    @Override
    public String createRefreshToken(@NonNull JwtAuthUserDetails userDetails) {
        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(userDetails.getUuid().toString())
                .expiration(getTokenExpirationAsDate(this.refreshTokenExpiration))
                .claim("type", "REFRESH")
                .signWith(signatureKeysProvider.privateKey())
                .compact();
    }

    private static Map<String, Object> extractAuthorities(Collection<? extends GrantedAuthority> grantedAuthorities) {
        final Set<String> roles = new HashSet<>();
        final Set<String> permissions = new HashSet<>();

        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            if (grantedAuthority instanceof Role role) {
                roles.add(role.getRoleName());
                continue;
            }

            if (grantedAuthority instanceof Permission permission) {
                permissions.add(permission.getPermission());
            }
        }

        return Map.of(
                "roles", roles,
                "permissions", permissions
        );
    }

    private static Date getTokenExpirationAsDate(Duration tokenExpiration) {
        Instant accessExpirationInstant = Instant.now()
                .plus(tokenExpiration)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        return Date.from(accessExpirationInstant);
    }
}
