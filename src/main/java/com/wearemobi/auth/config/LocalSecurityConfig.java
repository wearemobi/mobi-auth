// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.config;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@Profile("local")
public class LocalSecurityConfig {

  private static final Logger log = LoggerFactory.getLogger(LocalSecurityConfig.class);

  @Value("${mobi.auth.client-id}")
  private String clientId;

  @Value("${mobi.auth.client-secret}")
  private String clientSecret;

  @Value("${mobi.auth.redirect-uri}")
  private String redirectUri;

  @Bean
  @Order(1)
  public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) {
    var authServerConfigurer = new OAuth2AuthorizationServerConfigurer();
    var endpointsMatcher = authServerConfigurer.getEndpointsMatcher();
    return http.securityMatcher(endpointsMatcher)
        .with(
            authServerConfigurer,
            (authorizationServer) -> authorizationServer.oidc(Customizer.withDefaults()))
        .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
        .exceptionHandling(
            (exceptions) ->
                exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
        .build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
    return http.authorizeHttpRequests(
            (authorize) ->
                authorize
                    .requestMatchers("/login", "/css/**", "/images/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(form -> form.loginPage("/login").permitAll())
        .build();
  }

  @Bean
  public RegisteredClientRepository registeredClientRepository() {
    log.info("🚀 [MOBI-AUTH] Motor OIDC Inicializado para el cliente: {}", clientId);
    RegisteredClient oidcClient =
        RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(clientId)
            .clientSecret("{noop}" + clientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri(redirectUri)
            .scope(OidcScopes.OPENID)
            .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
            .build();

    return new InMemoryRegisteredClientRepository(oidcClient);
  }
}
