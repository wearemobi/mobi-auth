// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.component;

import com.wearemobi.auth.config.OciTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class OciTokenService {

  private static final Logger log = LoggerFactory.getLogger(OciTokenService.class);

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${oci.client.id}")
  private String clientId;

  @Value("${oci.client.secret}")
  private String clientSecret;

  @Value("${oci.token.url}")
  private String tokenUrl;

  @Value("${mobi.auth.scope}")
  private String scope;

  public String getM2mToken() {
    // Verificación de configuración antes de la petición a Oracle
    log.debug(
        "Initiating M2M token request to OCI. URL=[{}], ClientID=[{}], Scope=[{}]",
        tokenUrl,
        clientId,
        scope);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth(clientId, clientSecret);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "client_credentials");
    body.add("scope", scope);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

    try {
      var response = restTemplate.postForEntity(tokenUrl, request, OciTokenResponse.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        log.info("Successfully retrieved M2M token from OCI Identity Provider.");
        return response.getBody().accessToken();
      }

      log.warn("Received empty or non-OK response from OCI Identity Provider.");
      return null;
    } catch (Exception e) {
      log.error("OCI M2M token retrieval failed. Details: {}", e.getMessage());
      throw new RuntimeException(
          "Failed to retrieve M2M token from OCI Identity Provider: " + e.getMessage());
    }
  }
}
