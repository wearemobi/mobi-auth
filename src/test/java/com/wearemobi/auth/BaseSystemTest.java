// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/**
 * Clase base para pruebas de integración de M.O.B.I. Proporciona el contexto de Spring y las
 * variables de entorno 'dummy' para evitar la duplicación de código en las clases hijas.
 */
@SpringBootTest(
    properties = {
      "oci.client.id=dummy",
      "oci.client.secret=dummy",
      "oci.token.url=http://dummy",
      "mobi.auth.scope=dummy",
      "MOBI_OCI_SCIM_URL=http://dummy",
      "MOBI_OCI_TOKEN_URI=http://dummy",
      "MOBI_OCI_ISSUER_URI=http://dummy",
      "MOBI_AUTH_CLIENT_ID=dummy",
      "MOBI_AUTH_CLIENT_SECRET=dummy",
      "MOBI_M2M_CLIENT_ID=dummy",
      "MOBI_M2M_CLIENT_SECRET=dummy",
      "MOBI_JWT_SECRET=SuperSecretMobiKey2026NeedToBeLongEnough32Bytes",
      "MOBI_JWT_EXPIRATION_HOURS=24",
      "MOBI_JWT_REFRESH-EXPIRATION-DAYS=7"
    })
@AutoConfigureMockMvc
public abstract class BaseSystemTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
