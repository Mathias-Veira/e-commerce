package com.project.e_commerce.infrastructure.configuration;

import com.project.e_commerce.domain.ports.out.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class SecurityConfig {
    private final JWTService jwtService;
    private final HandlerExceptionResolver resolver;
    private final UserRepository userRepository;
    public SecurityConfig(JWTService jwtService, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,UserRepository userRepository){
        this.jwtService = jwtService;
        this.resolver = resolver;
        this.userRepository = userRepository;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        SecurityFilter securityFilter = new SecurityFilter(jwtService,resolver,userRepository);
        http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth->auth.requestMatchers("/api/users/login", "/api/users/auth/refresh", "/api/users/register").permitAll().requestMatchers("/api/products/register","/api/products/update").hasRole("ADMIN").requestMatchers(HttpMethod.DELETE,"/api/products/**").hasRole("ADMIN").requestMatchers("/api/categories/**").hasRole("ADMIN").anyRequest().authenticated()).addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

