package com.wearemobi.auth.config;

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

import java.util.Collections;
import java.util.Objects;

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

  @Value("${spring.security.oauth2.client.provider.oci.token-uri:/oauth2/v1/token}")
  private String tokenEndpoint;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    var username = authentication.getName();
    var password = Objects.requireNonNull(authentication.getCredentials()).toString();
    var tokenEndpointUri = issuerUri + tokenEndpoint;

    log.debug(">> OCI:Requesting token for user: {}, scope: {}", username, authScope);

    var restTemplate = new RestTemplate();
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth(clientId, clientSecret);

    var body = new LinkedMultiValueMap<String, String>();
    body.add("grant_type", "password");
    body.add("username", username);
    body.add("password", password);
    body.add("scope", authScope);

    try {
      var request = new HttpEntity<>(body, headers);
      // We now map directly to our Record!
      var response = restTemplate.postForEntity(tokenEndpointUri, request, OciTokenResponse.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        var tokenData = response.getBody();
        log.debug("✅ OCI:Authentication successful. Token captured for user: {}", username);

        // We store the OciTokenResponse as "details" so it's available in the SecurityContext
        var authResult = new UsernamePasswordAuthenticationToken(
                username,
                null, // Clear password after successful auth
                Collections.emptyList()
        );
        authResult.setDetails(tokenData);

        return authResult;
      }
    } catch (HttpClientErrorException e) {
      var errorBody = e.getResponseBodyAsString();
      log.error("❌ OCI:Auth failed [{}]: {}", e.getStatusCode(), errorBody);
      throw new BadCredentialsException("OCI Identity Provider rejected credentials.");
    } catch (Exception e) {
      log.error("❌ OCI:Critical system error during authentication: {}", e.getMessage());
      throw new BadCredentialsException("Internal Auth Bridge Failure.");
    }

    throw new BadCredentialsException("Could not complete authentication with OCI.");
  }

  @Override
  public boolean supports(@NonNull Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}