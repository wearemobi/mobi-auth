// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("oci")
public class OciSecurityConfig {

  private final OciAuthenticationProvider ociAuthenticationProvider;

  public OciSecurityConfig(OciAuthenticationProvider ociAuthenticationProvider) {
    this.ociAuthenticationProvider = ociAuthenticationProvider;
  }

  @Bean
  public SecurityFilterChain ociSecurityFilterChain(HttpSecurity http) {
    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/login", "/register", "/css/**", "/images/**", "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                // Jolly Roger B&W
                form.loginPage("/login").defaultSuccessUrl("/home", true).permitAll())
        .logout(LogoutConfigurer::permitAll)
        .authenticationProvider(ociAuthenticationProvider);

    return http.build();
  }

  @Bean
  public AuthenticationManager authManager(HttpSecurity http) {
    var authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder.authenticationProvider(ociAuthenticationProvider);
    return authenticationManagerBuilder.build();
  }
}
