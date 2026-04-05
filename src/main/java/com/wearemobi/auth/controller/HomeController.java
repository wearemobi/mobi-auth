// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.controller;

import com.wearemobi.auth.config.OciTokenResponse;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class HomeController {

  @GetMapping("/home")
  public String home(Model model) {
    var auth = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));

    var tokenData = Optional.ofNullable(auth.getDetails())
            .filter(OciTokenResponse.class::isInstance)
            .map(OciTokenResponse.class::cast)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Missing OCI token details"));

    model.addAttribute("username", auth.getName());
    model.addAttribute("status", "AUTH_VIA_MOBI_ON_OCI");
    model.addAttribute("tokenType", tokenData.tokenType());
    model.addAttribute("expiresIn", tokenData.expiresIn());

    return "home";
  }

  @GetMapping("/profile")
  public String profile(Model model) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    var tokenData = (OciTokenResponse) auth.getDetails();

    model.addAttribute("username", auth.getName());
    model.addAttribute("accessToken", tokenData.accessToken());
    model.addAttribute("idToken", tokenData.idToken());
    model.addAttribute("expiresIn", tokenData.expiresIn());

    return "profile";
  }
}
