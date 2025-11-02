package com.zidio.config;

import com.zidio.repository.UserRepository;
import com.zidio.security.JwtAuthFilter;
import com.zidio.security.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    public SecurityConfig(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils; this.userRepository = userRepository;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtils, userRepository);
        http.csrf().disable()
            .authorizeHttpRequests()
            .requestMatchers("/api/auth/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
