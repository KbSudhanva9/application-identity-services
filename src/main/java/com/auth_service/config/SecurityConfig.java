package com.auth_service.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth_service.security.JwtAccessDeniedHandler;
import com.auth_service.security.JwtAuthenticationEntryPoint;
import com.auth_service.security.JwtAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint authenticationEntryPoint;
	private final JwtAccessDeniedHandler accessDeniedHandler;

	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception {

        http
        	.csrf(csrf -> csrf.disable())
        	.sessionManagement( session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        	.exceptionHandling(ex -> ex
        	        .authenticationEntryPoint(authenticationEntryPoint)
        	        .accessDeniedHandler(accessDeniedHandler)
        	)
        	.authorizeHttpRequests(auth -> auth
        			.requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/auth/refresh"
//                                ,"/auth"
        					).permitAll()
        			.anyRequest().authenticated())
//        	.httpBasic(Customizer.withDefaults());
        	.addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}