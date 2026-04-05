// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable representation of the OCI IAM Token Response. Aligned with OIDC/OAuth2 specifications.
 */
public record OciTokenResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("id_token") String idToken,
    @JsonProperty("expires_in") Integer expiresIn,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("refresh_token") String refreshToken) {}
