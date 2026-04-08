// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // [FRANKY-DEBUG]: Enabling the radar to detect unauthorized boardings! AUUU!
@Profile("oci")
public class OciSecurityConfig {

  private final OciAuthenticationProvider ociAuthenticationProvider;

  public OciSecurityConfig(OciAuthenticationProvider ociAuthenticationProvider) {
    this.ociAuthenticationProvider = ociAuthenticationProvider;
  }

  @Value("${mobi.auth.security.public-routes:/login,/error}")
  private String[] publicRoutes;

  @Bean
  public SecurityFilterChain ociSecurityFilterChain(
      HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) {

    // CORS
    http.cors(org.springframework.security.config.Customizer.withDefaults());

    // CSRF
    http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/auth/**"));

    // AUTHORIZATION
    http.authorizeHttpRequests(
        auth -> auth.requestMatchers(publicRoutes).permitAll().anyRequest().authenticated());

    // FILTERS
    http.addFilterBefore(
        jwtAuthFilter,
        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

    // LOGIN FORM
    http.formLogin(
            form ->
                // Jolly Roger B&W
                form.loginPage("/login").defaultSuccessUrl("/home", true).permitAll())
        .logout(LogoutConfigurer::permitAll);

    return http.authenticationProvider(ociAuthenticationProvider).build();
  }

  @Bean
  public AuthenticationManager authManager(HttpSecurity http) {
    var authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder.authenticationProvider(ociAuthenticationProvider);
    return authenticationManagerBuilder.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
