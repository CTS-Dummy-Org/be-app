package com.cognizant.quickstart.messages.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cognizant.quickstart.messages.domain.Message;
import com.cognizant.quickstart.messages.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@AutoConfigureMockMvc
@ContextConfiguration(classes = {MessageController.class})
@WebMvcTest
public class MessageControllerTest {

  @InjectMocks
  MessageController messageController;
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private MessageService messageService;
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
  public void shouldTestCreateMessageOfController() throws Exception {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    Message message = new Message();
    message.setMessage("Hello, Message");
    message.setUserId("userId123");
    message.setActive(false);
    message.setId("id");
    message.setCreatedAt(LocalDateTime.now());
    message.setUpdatedAt(LocalDateTime.now());
    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    User principal = new User("user", "password", authorities);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(principal, null, authorities);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    when(messageService.createMessage(any())).thenReturn(message);
    this.mockMvc.perform(
            post("/api/v1/messages").with(user(principal)).content(asJsonString(message))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  public void deleteMessageOfController() throws Exception {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    this.mockMvc.perform(delete("/api/v1/messages/id").contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());
  }

  @Test
  public void shouldTestModifyMessageOfController() throws Exception {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    Message message = new Message();
    message.setMessage("Hello, Message");
    message.setUserId("userId123");
    message.setActive(false);
    message.setId("id");
    message.setCreatedAt(LocalDateTime.now());
    message.setUpdatedAt(LocalDateTime.now());
    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    User principal = new User("user", "password", authorities);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(principal, null, authorities);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    when(messageService.modifyMessage(any(), any(), any())).thenReturn(message);
    this.mockMvc.perform(
            put("/api/v1/messages/id").with(user(principal)).content(asJsonString(message))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(username = "admin", authorities = {"ADMIN", "USER"})
  public void shouldTestGetMessageOfController() throws Exception {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    Message message = new Message();
    message.setMessage("Hello, Message");
    message.setUserId("userId123");
    message.setActive(false);
    message.setId("id");
    message.setCreatedAt(LocalDateTime.now());
    message.setUpdatedAt(LocalDateTime.now());
    this.mockMvc.perform(get("/api/v1/messages")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "admin", authorities = {"ADMIN", "USER"})
  public void shouldTestGetMessageWithParamOfController() throws Exception {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    Message message = new Message();
    message.setMessage("Hello, Message");
    message.setUserId("userId123");
    message.setActive(false);
    message.setId("id");
    message.setCreatedAt(LocalDateTime.now());
    message.setUpdatedAt(LocalDateTime.now());
    this.mockMvc.perform(get("/api/v1/messages?active=true")).andExpect(status().isOk());
  }
}
