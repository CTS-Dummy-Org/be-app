package com.cognizant.quickstart.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Getter
@PropertySource("classpath:application.properties")
public class ConfigProperties {

  @Value("${CORS_URL}")
  private String corsUrl;

  @Value("${USER_POOL_ID}")
  private String userPoolID;

  @Value("${OAUTH_IDP_HOST}")
  private String oauthIdpHost;

  @Value("${OAUTH_IDP_CLIENT_ID}")
  private String oauthIdpClientId;

  @Value("${OAUTH_REDIRECTURI}")
  private String oauthRedirectURI;

  @Value("${OAUTH_IDP_ISSUER}")
  private String oauthIdpIssuer;

  @Value("${OAUTH_IDP_AUTH_END_POINT}")
  private String oauthIdpAuthEndPoint;

  @Value("${OAUTH_IDP_TOKEN_END_POINT}")
  private String oauthIdpTokenEndPoint;

  @Value("${OAUTH_IDP_END_SESSION_END_POINT}")
  private String oauthIdpEndSessionEndPoint;

  @Value("${CERT_URL}")
  private String certUrl;

}
