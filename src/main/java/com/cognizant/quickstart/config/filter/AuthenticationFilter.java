package com.cognizant.quickstart.config.filter;

import com.cognizant.quickstart.config.ConfigProperties;
import com.cognizant.quickstart.constant.Constants;
import com.cognizant.quickstart.util.CustomSigningKeyResolver;
import com.cognizant.quickstart.util.TokenType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


@Slf4j
public class AuthenticationFilter extends BasicAuthenticationFilter {

  private final ConfigProperties properties;

  public AuthenticationFilter(
      AuthenticationManager authenticationManager, ConfigProperties config) {
    super(authenticationManager);
    this.properties = config;
  }

  @SuppressWarnings("checkstyle:LocalVariableName")
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    String[] publicURL = {"/api/v1/identityConfig", "/actuator/health", "/healthcheck"};
    if (Arrays.stream(publicURL).anyMatch(url -> url.equals(request.getServletPath()))) {
      filterChain.doFilter(request, response);
    } else {
      String token = request.getHeader(Constants.AUTHORIZATION);
      if (token != null) {
        try {
          token = token.replace(Constants.TOKEN_PREFIX, "");
          SigningKeyResolver signingKeyResolver = getMySigningKeyResolver();

          // DONE : 1. The audience (aud) claim should match the app client
          // ID that was created in the Amazon Cognito user pool.
          // DONE : 2. The issuer (iss) claim should match your user pool.
          // DONE : 3. Verify that the token is not expired.
          // DONE : 4. Check the token_use claim.the token_use claim must be either id or access.
          // DONE : 5. Proper Error Handling and Response Status with Message
          // and Check if Algorithm is RSA-SHA256 if not then throw error
          int index = token.lastIndexOf('.');
          String tokenWithOutSignature = token.substring(0, index + 1);
          String algo =
              Jwts.parserBuilder().build().parseClaimsJwt(tokenWithOutSignature)
                  .getHeader().get("alg").toString();
          if (algo.equals("RS256")) {
            // "aud" for id_Token is 'aud' whereas in access_Token
            // it is 'client_id' , so need to read token_use to verify it's Audience
            String tokenType =
                Jwts.parserBuilder().build()
                    .parseClaimsJwt(tokenWithOutSignature).getBody().get("token_use")
                    .toString();
            String claimName = tokenType.equals(TokenType.id.name()) ? "aud" :
                tokenType.equals(TokenType.access.name()) ? "client_id" : null;
            Jws<Claims>
                claims =
                Jwts.parserBuilder().require(claimName, properties.getOauthIdpClientId())
                    .requireIssuer(properties.getOauthIdpIssuer())
                    .setSigningKeyResolver(signingKeyResolver).build()
                    .parseClaimsJws(token);
            String sub = claims.getBody()
                .getSubject();
            // currently their is no roles attached to token , so default ROLE_USER is provided .
            // if required then just add roles from token using
            // claims.getBody().get("${ROLE KEY attribute name in JWT TOKEN}");
            List<String> roles = List.of("ROLE_USER");
            Collection<SimpleGrantedAuthority> authority = new ArrayList<>();
            roles.forEach(role -> authority.add(new SimpleGrantedAuthority(role)));
            User principal = new User(sub, "", authority);
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authority);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
          } else {
            createTokenErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                "token is not signed using RS256 algorithm", response);
          }
        } catch (JwtException e) {
          createTokenErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage(), response);
        }
      } else {
        createTokenErrorResponse(HttpStatus.UNAUTHORIZED.value(), "No Token Found", response);
      }
    }
  }

  private SigningKeyResolver getMySigningKeyResolver() {
    return new CustomSigningKeyResolver(properties);
  }


  private void createTokenErrorResponse(int code, String message, HttpServletResponse res)
      throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    res.setStatus(HttpStatus.UNAUTHORIZED.value());
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    res.getWriter().print(
        objectMapper
            .writeValueAsString(Map.of("error", Map.of("code", code, "message", message))));
  }

}
