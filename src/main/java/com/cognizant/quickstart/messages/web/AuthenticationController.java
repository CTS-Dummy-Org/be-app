package com.cognizant.quickstart.messages.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class AuthenticationController {

  @GetMapping("/authorized")
  public ResponseEntity<String> getAuthorized() {
    log.debug("Get user authorized api");
    return new ResponseEntity<>("Authorized", HttpStatus.OK);
  }
}
