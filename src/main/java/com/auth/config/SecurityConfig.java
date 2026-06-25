package com.auth.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth.security.JwtAccessDeniedHandler;
import com.auth.security.JwtAuthenticationEntryPoint;
import com.auth.security.JwtAuthenticationFilter;


@Configuration
public class SecurityConfig {

	@Autowired
	private   JwtAuthenticationFilter jwtAuthenticationFilter;
	@Autowired
	private   JwtAuthenticationEntryPoint authenticationEntryPoint;
	@Autowired
	private   JwtAccessDeniedHandler accessDeniedHandler;

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
        					 "/auth/request-otp",
        					 "/auth/verify-otp",
                             "/auth/register",
                             "/auth/login",
                             "/auth/refresh",
                             "/auth/session/**",
                             "/auth/valid-user",
                             "/auth/reset-password-otp",
                             "/auth/reset-password"
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