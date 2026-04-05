// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.component;

import com.wearemobi.auth.domain.ScimUserRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OciIdentityService {

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${oci.scim.url}")
  private String scimUrl;

  public void createUser(String email, String fullName, String password, String m2mToken) {
    var request = ScimUserRequest.create(email, fullName, password);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(m2mToken);

    HttpEntity<ScimUserRequest> entity = new HttpEntity<>(request, headers);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(scimUrl, entity, String.class);
      if (response.getStatusCode() == HttpStatus.CREATED) {
        System.out.println("¡AUUU! Usuario creado con éxito en OCI.");
      }
    } catch (Exception e) {
      // Lógicamente, Robin se encargará de analizar estos errores después
      throw new RuntimeException("Error al reclutar tripulante: " + e.getMessage());
    }
  }
}
