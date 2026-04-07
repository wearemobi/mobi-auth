// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // 🛠️ DEBUG: Enables @Async for non-blocking hooks
public class MobiAuthApplication {

  public static void main(String[] args) {
    SpringApplication.run(MobiAuthApplication.class, args);
  }
}
