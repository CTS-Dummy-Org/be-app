package com.cognizant.quickstart.controller;

import com.cognizant.quickstart.config.ConfigProperties;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class AuthController {

  @Autowired
  ConfigProperties properties;

  @GetMapping(path = "/identityConfig", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getIdentityConfig() {
    Map<String, String> data = new HashMap<>();
    data.put("idpHost", properties.getOauthIdpHost());
    data.put("clientId", properties.getOauthIdpClientId());
    data.put("authorizationEndpoint", properties.getOauthIdpAuthEndPoint());
    data.put("issuer", properties.getOauthIdpIssuer());
    data.put("tokenEndpoint", properties.getOauthIdpTokenEndPoint());
    data.put("redirectURI", properties.getOauthRedirectURI());
    data.put("endSessionEndpoint", properties.getOauthIdpEndSessionEndPoint());
    data.put("certUrl", properties.getCertUrl());
    return ResponseEntity.ok(data);
  }

  // JUST FOR TESTING PURPOSE
  @GetMapping("/secure")
  public ResponseEntity<Object> getAuth() {
    return ResponseEntity.ok("Authorized");
  }
}
