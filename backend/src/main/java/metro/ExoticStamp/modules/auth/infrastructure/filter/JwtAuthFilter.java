package metro.ExoticStamp.modules.auth.infrastructure.filter;

import metro.ExoticStamp.modules.auth.domain.exception.InvalidTokenException;
import metro.ExoticStamp.modules.auth.domain.exception.TokenExpiredException;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.ParsedAccessToken;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationStatus;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final RoleQueryService roleQueryService;
    private final AccessTokenRevocationValidator accessTokenRevocationValidator;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());

        try {
            ParsedAccessToken parsed = jwtProvider.parseAccessToken(token);
            if (accessTokenRevocationValidator.validate(parsed.userId(), parsed.jti(), parsed.tokenVersion())
                    == AccessTokenRevocationStatus.REVOKED) {
                log.debug("[JwtAuthFilter] access token revoked userId={}", parsed.userId());
            } else if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(parsed.userId().toString());
                Collection<? extends GrantedAuthority> authorities =
                        resolveAuthoritiesFromDb(parsed.userId());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (InvalidTokenException | TokenExpiredException e) {
            log.debug("[JwtAuthFilter] token processing failed err={}", e.getMessage());
        } catch (Exception e) {
            log.debug("[JwtAuthFilter] token processing failed err={}", e.getMessage());
        }

        chain.doFilter(request, response);
    }

    /**
     * Roles: DB codes like {@code ADMIN} → {@code ROLE_ADMIN} for {@code hasRole}.
     * Permissions: stored as plain authority strings for {@code hasAuthority}.
     */
    private List<GrantedAuthority> resolveAuthoritiesFromDb(UUID userId) {
        List<GrantedAuthority> roles = roleQueryService.getRoleNamesByUserId(userId).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::toRoleGrantedAuthority)
                .toList();
        List<GrantedAuthority> permissions = roleQueryService.getPermissionCodesByUserId(userId).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                .toList();
        return Stream.concat(roles.stream(), permissions.stream())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private GrantedAuthority toRoleGrantedAuthority(String roleName) {
        if (roleName.startsWith(ROLE_PREFIX)) {
            return new SimpleGrantedAuthority(roleName);
        }
        return new SimpleGrantedAuthority(ROLE_PREFIX + roleName);
    }
}

