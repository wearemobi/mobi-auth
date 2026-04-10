// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.component;

import com.wearemobi.auth.edge.EdgeProfile;
import com.wearemobi.auth.entity.UserEntity;
import com.wearemobi.auth.event.UserRegisteredEvent;
import com.wearemobi.auth.mapper.EdgeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CloudflareSyncService {

  private static final Logger log = LoggerFactory.getLogger(CloudflareSyncService.class);

  private final RestClient restClient;

  @Value("${cloudflare.api-token}")
  private String apiToken;

  @Value("${cloudflare.account-id}")
  private String accountId;

  @Value("${cloudflare.namespace-id}")
  private String namespaceId;

  // Franky's Constructor: Forjando el cañón principal
  public CloudflareSyncService() {
    this.restClient = RestClient.create("https://api.cloudflare.com/client/v4");
  }

  @Async
  @EventListener
  public void handleUserSync(UserRegisteredEvent event) {
    UserEntity user = event.user();
    log.info("[EDGE-SYNC] Synchronizing profile to Cloudflare KV for: {}", user.getEmail());

    try {
      EdgeProfile profile = EdgeMapper.toEdgeProfile(user);
      String uri =
          String.format(
              "/accounts/%s/storage/kv/namespaces/%s/values/%s",
              accountId, namespaceId, profile.slug());

      restClient
          .put()
          .uri(uri)
          .header("Authorization", "Bearer " + apiToken)
          .header("Content-Type", "application/json")
          .body(profile)
          .retrieve()
          .toBodilessEntity();

      log.info("[EDGE-SYNC] Successfully anchored profile [{}] to Cloudflare KV.", profile.slug());
    } catch (Exception e) {
      log.error("[EDGE-SYNC] Failed to sync profile for [{}]: {}", user.getEmail(), e.getMessage());
    }
  }
}
