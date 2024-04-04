package com.cognizant.quickstart.messages.web;

import com.cognizant.quickstart.messages.domain.Message;
import com.cognizant.quickstart.messages.service.MessageService;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriBuilder;

@RestController
@RequestMapping("/api/v1/messages")
@Slf4j
public class MessageController {

  @Autowired
  MessageService messageService;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> createMessage(@RequestBody Message message) {
    log.info("Adding Message '{}' to DB.", message.getMessage());
    message.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
    Message msg = messageService.createMessage(message);
    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(msg.getId())
        .toUri();
    return ResponseEntity.created(location).build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteMessage(@PathVariable String id) {
    messageService.deleteMessage(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> modifyMessage(@RequestBody Message message, @PathVariable String id) {
    log.info("Modifying Message '{}' to DB.", message.getMessage());
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    messageService.modifyMessage(message, id, userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getAllMessages(@RequestParam Optional<Boolean> active) {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    List<Message> messages =
        messageService.getAllMessages(userId, active.isEmpty() ? null : active.get());
    return ResponseEntity.ok(messages);
  }
}
