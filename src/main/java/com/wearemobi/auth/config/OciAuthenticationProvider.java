// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.config;

import com.wearemobi.auth.component.JwtService;
import com.wearemobi.auth.domain.MobiUser;
import com.wearemobi.auth.entity.UserEntity;
import com.wearemobi.auth.mapper.UserMapper;
import com.wearemobi.auth.repository.UserRepository;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Profile("oci")
public class OciAuthenticationProvider implements AuthenticationProvider {

  private static final Logger log = LoggerFactory.getLogger(OciAuthenticationProvider.class);

  @Value("${spring.security.oauth2.client.registration.oci.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.oci.client-secret}")
  private String clientSecret;

  @Value("${mobi.auth.scope}")
  private String authScope;

  @Value("${spring.security.oauth2.client.provider.oci.token-uri:/oauth2/v1/token}")
  private String tokenEndpointUri;

  private final UserRepository userRepository;
  private final JwtService jwtService;

  public OciAuthenticationProvider(UserRepository userRepository, JwtService jwtService) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    var username = authentication.getName();
    var password = Objects.requireNonNull(authentication.getCredentials()).toString();

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
      var response = restTemplate.postForEntity(tokenEndpointUri, request, OciTokenResponse.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        log.debug("✅ OCI:Authentication successful for user: {}", username);

        // --- The M.O.B.I. Broker Intercept ---
        Optional<UserEntity> userOpt = userRepository.findByEmail(username);

        if (userOpt.isEmpty()) {
          // Logic: If OCI approves but the user is missing from the MOBI DB, they are likely a new
          // registration without a Tenant.
          // For the MVP, we throw a clear exception rather than returning a basic token.
          log.warn("⚠️ MOBI: User {} authenticated in OCI but not found in Tenant DB.", username);
          throw new BadCredentialsException("Account not fully provisioned in M.O.B.I. system.");
        }

        UserEntity userEntity = userOpt.get();
        var mobiUser = UserMapper.toDomain(userEntity);

        // Generate the MOBI Domain JWT
        String mobiJwt = jwtService.generateToken(mobiUser);
        log.debug("🔥 MOBI: Super Token generated for tenant: {}", mobiUser.tenantId());

        return getUsernamePasswordAuthenticationToken(mobiUser, username, mobiJwt);
      }
    } catch (HttpClientErrorException e) {
      log.error("❌ OCI:Auth failed [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
      throw new BadCredentialsException("OCI Identity Provider rejected credentials.");
    } catch (Exception e) {
      log.error("❌ OCI:Critical system error during authentication: {}", e.getMessage());
      throw new BadCredentialsException("Internal Auth Bridge Failure.");
    }

    throw new BadCredentialsException("Could not complete authentication with OCI.");
  }

  private static @NonNull
      UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(
          MobiUser mobiUser, String username, String mobiJwt) {
    // Assign the role as a Spring Security GrantedAuthority to protect routes
    var authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + mobiUser.role()));
    var authResult =
        new UsernamePasswordAuthenticationToken(
            username,
            null, // Clear password after successful auth
            authorities);

    // Store the original OCI token and the new M.O.B.I. Token in the details
    // (A custom Record could be implemented here if both tokens need to be accessed later)
    authResult.setDetails(mobiJwt);
    return authResult;
  }

  @Override
  public boolean supports(@NonNull Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
