// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.controller;

import com.wearemobi.auth.config.OciTokenResponse;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class HomeController {

  @GetMapping("/home")
  public Map<String, Object> home() {
    var auth =
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));

    var tokenData =
        Optional.ofNullable(auth.getDetails())
            .filter(OciTokenResponse.class::isInstance)
            .map(OciTokenResponse.class::cast)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Missing OCI token details"));

    return Map.of(
        "message", "All Blue! Welcome to M.O.B.I.™!",
        "user", auth.getName(),
        "status", "AUTH_VIA_MOBI_ON_OCI",
        "token_type", tokenData.tokenType(),
        "expires_in", tokenData.expiresIn());
  }
}
