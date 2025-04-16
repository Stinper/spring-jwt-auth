package me.stinper.jwtauth.core.security.jwt;

import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.core.security.jwt.service.JwtClaimsService;
import me.stinper.jwtauth.core.security.jwt.service.JwtVerificationService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtClaimsService jwtClaimsService;
    private final JwtVerificationService jwtVerificationService;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = this.getTokenFromRequest(request);

            if (token != null) {
                Claims tokenClaims = jwtClaimsService.parseTokenClaims(token);

                final String tokenType = tokenClaims.get("type", String.class);

                if (tokenType == null) {
                    log.atError().log("[#doFilterInternal]: Предоставленный токен не имеет в полезной нагрузке обязательного поля 'type' " +
                            "\n\tЗначение токена: {}", token
                    );

                    throw new JwtException("");
                }

                if (!tokenType.equals("ACCESS")) {
                    log.atWarn().log("""
                            [#doFilterInternal]: Тип токена не соответствует ожидаемому\s
                            \tОжидался тип: ACCESS\s
                            \tФактический тип: {}""", tokenType
                    );

                    throw new JwtException("");
                }

                jwtVerificationService.verifyTokenSignature(token);

                log.atDebug().log("[#doFilterInternal]: Access-токен успешно верифицирован \n\tЗначение токена: {}", token);

                UserDetails userDetails = userDetailsService.loadUserByUsername(tokenClaims.get("email", String.class));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        }
        catch (JwtException jwtException) {
            this.handlerExceptionResolver.resolveException(request, response, null, jwtException);
        }
    }

    @Nullable
    private String getTokenFromRequest(@NonNull HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;
    }
}
