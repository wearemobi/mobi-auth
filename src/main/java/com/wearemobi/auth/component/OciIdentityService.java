// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.component;

import com.wearemobi.auth.domain.Role;
import com.wearemobi.auth.domain.ScimUserRequest;
import com.wearemobi.auth.entity.UserEntity;
import com.wearemobi.auth.event.UserRegisteredEvent;
import com.wearemobi.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Set;
import java.util.UUID;

@Service
public class OciIdentityService {

  private static final Logger log = LoggerFactory.getLogger(OciIdentityService.class);

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${oci.scim.url}")
  private String scimUrl;

  private final UserRepository userRepository; // Necesitamos guardar localmente
  private final ApplicationEventPublisher eventPublisher; // Necesitamos disparar el cañón

  public OciIdentityService(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
    this.userRepository = userRepository;
    this.eventPublisher = eventPublisher;
  }

  public void createUser(String email, String fullName, String password, String m2mToken) {
    var request = ScimUserRequest.create(email, fullName, password);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(m2mToken);

    HttpEntity<ScimUserRequest> entity = new HttpEntity<>(request, headers);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(scimUrl, entity, String.class);
      if (response.getStatusCode() == HttpStatus.CREATED) {
        log.info("Successfully provisioned user in OCI Identity: {}", email);

        UserEntity newUser = new UserEntity();
        newUser.setEmail(email);
        newUser.setOrgId(UUID.randomUUID());
        newUser.setTenantId(email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "-").toLowerCase());
        newUser.setOrgName("Workspace " + newUser.getTenantId());
        newUser.setRoles(Set.of(Role.MOBI_TENANT_USER));

        UserEntity savedUser = userRepository.save(newUser);
        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser));
      }
    } catch (Exception e) {
      log.error("Failed to provision OCI user [{}]: {}", email, e.getMessage());
      throw new RuntimeException("OCI Provisioning Error", e);
    }
  }
}
