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
    // 2. Robin: Verificamos los planos antes de arrancar
    log.info("🧪 MOBI Franky-Debug: Iniciando ignición M2M hacia OCI.");
    log.info("📡 Config: URL=[{}], ClientID=[{}], Scope=[{}]", tokenUrl, clientId, scope);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth(clientId, clientSecret);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "client_credentials");
    body.add("scope", scope);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

    try {
      log.info("🚀 MOBI Franky-Debug: Lanzando petición de energía a Oracle...");

      var response = restTemplate.postForEntity(tokenUrl, request, OciTokenResponse.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        log.info("⚡ MOBI Franky-Debug: ¡AUUU! Energía capturada. Token recibido con éxito.");
        return response.getBody().accessToken();
      }

      return null;
    } catch (Exception e) {
      // 3. Zoro: Si el motor estalla, cortamos el ruido y vemos la herida
      log.error(
          "💥 MOBI Franky-Debug: ¡ALERTA! El motor de energía falló. Detalles: {}", e.getMessage());
      throw new RuntimeException("¡AUUU! Fallo en el motor de energía M2M: " + e.getMessage());
    }
  }
}
