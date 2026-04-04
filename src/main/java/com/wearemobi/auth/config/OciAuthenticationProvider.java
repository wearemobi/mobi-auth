// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.config;

import java.util.Collections;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Profile("oci")
public class OciAuthenticationProvider implements AuthenticationProvider {

  private static final Logger log = LoggerFactory.getLogger(OciAuthenticationProvider.class);

  @Value("${spring.security.oauth2.client.provider.oci.issuer-uri}")
  private String issuerUri;

  @Value("${spring.security.oauth2.client.registration.oci.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.oci.client-secret}")
  private String clientSecret;

  @Value("${mobi.auth.scope:urn:opc:idm:__myscopes__}")
  private String authScope;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    var username = authentication.getName();
    var password = Objects.requireNonNull(authentication.getCredentials()).toString();
    var tokenEndpoint = issuerUri + "/oauth2/v1/token";

    log.debug(
        ">> OCI:authenticate {}, scope: {}, tokenEndpoint: {}", username, authScope, tokenEndpoint);

    var restTemplate = new RestTemplate();
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth(clientId, clientSecret);

    var body = new LinkedMultiValueMap<>();
    body.add("grant_type", "password");
    body.add("username", username);
    body.add("password", password);
    body.add("scope", authScope);

    try {
      var request = new HttpEntity<>(body, headers);
      var response = restTemplate.postForEntity(tokenEndpoint, request, String.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        log.debug(">> OCI:authenticate successful for username: {}", username);
        return new UsernamePasswordAuthenticationToken(username, password, Collections.emptyList());
      }
    } catch (HttpClientErrorException e) {
      var errorBody = e.getResponseBodyAsString();
      log.error(
          ">> OCI:authenticate HttpClientError, statusCode: {}, errorBody: {}",
          e.getStatusCode(),
          errorBody);
      throw new BadCredentialsException("OCI:authenticate HttpClientError", e);
    } catch (Exception e) {
      log.error(">> OCI:authenticate Exception , message: {}", e.getMessage());
      throw new BadCredentialsException("OCI:authenticate Exception", e);
    }
    throw new BadCredentialsException("OCI:authenticate UnknownError");
  }

  @Override
  public boolean supports(@NonNull Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
