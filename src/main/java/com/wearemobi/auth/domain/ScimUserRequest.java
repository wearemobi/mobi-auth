// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.domain;

import java.util.List;

public record ScimUserRequest(
    List<String> schemas,
    String userName,
    Name name,
    List<Email> emails,
    String password,
    boolean active) {
  public static ScimUserRequest create(String email, String fullName, String password) {
    return new ScimUserRequest(
        List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
        email,
        new Name(fullName, "MOBI_MEMBER"),
        List.of(new Email(email, "work", true)),
        password,
        true);
  }

  public record Name(String givenName, String familyName) {}

  public record Email(String value, String type, boolean primary) {}
}
