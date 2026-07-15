package com.project.e_commerce.infrastructure.configuration;


import com.project.e_commerce.application.exceptions.user.UserNotFoundException;
import com.project.e_commerce.domain.exceptions.UserNotValidException;
import com.project.e_commerce.domain.ports.out.UserRepository;
import com.project.e_commerce.infrastructure.persistence.user.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final HandlerExceptionResolver resolver;
    private final UserRepository userRepository;

    public SecurityFilter(JWTService jwtService, HandlerExceptionResolver resolver,UserRepository userRepository) {
        this.jwtService = jwtService;
        this.resolver = resolver;
        this.userRepository = userRepository;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            if (jwtService.isValid(token) && !jwtService.isRefreshtoken(token)) {
                String userEmail = jwtService.extractUserEmail(token);
                Set<Role> roles = userRepository.getUserRolesByEmail(userEmail);
                if(roles.isEmpty()) throw new UserNotFoundException("User is not found");
                List<GrantedAuthority> authorities = roles.stream().map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toList());
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userEmail, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                resolver.resolveException(request, response, null, new UserNotValidException("No valid token"));
                return;

            }
        }
        filterChain.doFilter(request, response);
    }
}
