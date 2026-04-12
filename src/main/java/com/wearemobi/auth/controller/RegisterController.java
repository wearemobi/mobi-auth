// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.controller;

import com.wearemobi.auth.component.OciIdentityService;
import com.wearemobi.auth.component.OciTokenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // <--- Importante
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

  private final OciTokenService tokenService;
  private final OciIdentityService identityService;

  public RegisterController(OciTokenService tokenService, OciIdentityService identityService) {
    this.tokenService = tokenService;
    this.identityService = identityService;
  }

  @GetMapping("/register")
  public String register() {
    return "register";
  }

  @PostMapping("/register")
  public String handleRegistration(
      @RequestParam String displayName,
      @RequestParam String username,
      @RequestParam String password,
      Model model,
      RedirectAttributes redirectAttributes) {

    try {
      String m2mToken = tokenService.getM2mToken();
      identityService.createUser(username, displayName, password, m2mToken);

      redirectAttributes.addFlashAttribute("message", "¡Welcome to the Crew! Sign in to start.");
      return "redirect:login";

    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      model.addAttribute("displayName", displayName);
      model.addAttribute("username", username);
      return "register";
    }
  }
}
