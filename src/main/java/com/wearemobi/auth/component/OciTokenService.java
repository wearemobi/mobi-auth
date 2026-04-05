// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.component;

import com.wearemobi.auth.config.OciTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class OciTokenService {

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
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth(clientId, clientSecret);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "client_credentials");
    body.add("scope", scope);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

    try {
      var response = restTemplate.postForEntity(tokenUrl, request, OciTokenResponse.class);
      return response.getBody().accessToken();
    } catch (Exception e) {
      throw new RuntimeException("¡AUUU! Fallo en el motor de energía M2M: " + e.getMessage());
    }
  }
}
