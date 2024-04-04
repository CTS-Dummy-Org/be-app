package com.cognizant.quickstart.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cognizant.quickstart.config.ConfigProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@AutoConfigureMockMvc
@ContextConfiguration(classes = {AuthController.class})
@WebMvcTest
public class AuthControlTest {

  @InjectMocks
  AuthController authController;
  @MockBean
  ConfigProperties configProperties;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private WebApplicationContext wac;

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void shouldTest_getAuth() throws Exception {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    ResultActions actions = this.mockMvc.perform(get("/api/v1/secure")).andDo(print())
        .andExpect(status().isOk());
    MvcResult result = actions.andReturn();
    assertEquals(result.getResponse().getContentAsString(), "Authorized");
    assertEquals(200, result.getResponse().getStatus());
  }

  @Test
  public void shouldTest_getIdentityConfig() throws Exception {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    Map<String, String> data = new HashMap<>();
    data.put("idpHost", configProperties.getOauthIdpHost());
    data.put("clientId", configProperties.getOauthIdpClientId());
    data.put("authorizationEndpoint", configProperties.getOauthIdpAuthEndPoint());
    data.put("issuer", configProperties.getOauthIdpIssuer());
    data.put("tokenEndpoint", configProperties.getOauthIdpTokenEndPoint());
    data.put("redirectURI", configProperties.getOauthRedirectURI());
    data.put("endSessionEndpoint", configProperties.getOauthIdpEndSessionEndPoint());
    ResultActions actions =
        this.mockMvc.perform(get("/api/v1/identityConfig").content(asJsonString(data))
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());
    MvcResult result = actions.andReturn();
    assertEquals(200, result.getResponse().getStatus());
  }
}
