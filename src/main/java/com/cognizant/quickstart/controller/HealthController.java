package com.cognizant.quickstart.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping()
public class HealthController {
  @GetMapping("/healthcheck")
  public ResponseEntity<Object> getHealth() {
    return ResponseEntity.ok("API is running and is accessible");
  }
}
