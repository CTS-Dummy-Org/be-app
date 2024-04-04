package com.cognizant.quickstart.util;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.cognizant.quickstart.config.ConfigProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomSigningKeyResolver extends SigningKeyResolverAdapter {

  private final ConfigProperties properties;

  public CustomSigningKeyResolver(ConfigProperties properties) {
    this.properties = properties;
  }

  @Override
  public Key resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
    String keyId = jwsHeader.getKeyId();
    String issuer = claims.getIssuer();
    return lookupVerificationKey(keyId, issuer);
  }

  private PublicKey lookupVerificationKey(String keyId, String oauthIssuer) {
    // DONE:  fetch Public Keys from IDP_ISSUER URL and 
    //cross-check with keyId and return that Public key

    JwkProvider provider;
    Jwk jwk;
    RSAPublicKey publicKey;
    try {
      provider = new UrlJwkProvider(new URL(properties.getCertUrl()));
      jwk = provider.get(keyId);
      publicKey = (RSAPublicKey) jwk.getPublicKey();

    } catch (MalformedURLException | JwkException e) {
      log.error("error in fetching public key : {}", e.getMessage());
      return null;
    }
    return publicKey;
  }
}
