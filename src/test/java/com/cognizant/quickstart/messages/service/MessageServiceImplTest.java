package com.cognizant.quickstart.messages.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cognizant.quickstart.messages.data.MessageRepository;
import com.cognizant.quickstart.messages.domain.Message;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class MessageServiceImplTest {
  @Mock
  private MessageRepository messageRepository;
  @InjectMocks
  private MessageServiceImpl messageService;

  @Test
  public void shouldCreateMessageTest() {
    Message message = new Message();
    message.setMessage("Hello, Message");
    message.setUserId("userId123");
    message.setActive(false);
    message.setId("id");
    message.setCreatedAt(LocalDateTime.now());
    message.setUpdatedAt(LocalDateTime.now());
    when(messageRepository.save(any())).thenReturn(message);
    Message msg = messageService.createMessage(message);
    Assertions.assertNotNull(msg);
  }

  @Test
  public void shouldGetActiveMessageTest() {
    Message message = new Message();
    message.setMessage("Hello, Message");
    message.setUserId("userId123");
    message.setActive(true);
    message.setId("id");
    message.setCreatedAt(LocalDateTime.now());
    message.setUpdatedAt(LocalDateTime.now());
    List<Message> messages = new ArrayList<>();
    messages.add(message);
    when(messageRepository.findByUserIdAndActive("userId123", true)).thenReturn(messages);
    List<Message> msg = messageService.getAllMessages("userId123", true);
    Assertions.assertNotNull(msg);
  }

  @Test
  public void shouldGetAllMessageTest() {
    Message message = new Message();
    message.setMessage("Hello, Message");
    message.setUserId("userId123");
    message.setActive(true);
    message.setId("id");
    message.setCreatedAt(LocalDateTime.now());
    message.setUpdatedAt(LocalDateTime.now());
    List<Message> messages = new ArrayList<>();
    messages.add(message);
    when(messageRepository.findByUserId("userId123")).thenReturn(messages);
    List<Message> msg = messageService.getAllMessages("userId123", null);
    Assertions.assertNotNull(msg);
  }

  @Test
  public void deleteMessageTest() {
    Message message = new Message();
    message.setMessage("Hello, Message");
    message.setUserId("userId123");
    message.setActive(false);
    message.setId("id");
    message.setCreatedAt(LocalDateTime.now());
    message.setUpdatedAt(LocalDateTime.now());
    when(messageRepository.findById(any())).thenReturn(Optional.of(message));
    Message msg = messageService.deleteMessage(message.getId());
    Assertions.assertNotNull(msg);
  }

  @Test
  public void shouldModifyMessageTest() {
    Message message = new Message();
    message.setMessage("Hello, Message");
    message.setUserId("userId123");
    message.setActive(false);
    message.setId("id");
    message.setCreatedAt(LocalDateTime.now());
    message.setUpdatedAt(LocalDateTime.now());
    when(messageRepository.findById(any())).thenReturn(Optional.of(message));
    Message prevMsg = new Message();
    prevMsg.setMessage("Hello, Prev");
    prevMsg.setUserId("userId123");
    prevMsg.setActive(true);
    prevMsg.setId("id");
    prevMsg.setCreatedAt(LocalDateTime.now());
    prevMsg.setUpdatedAt(LocalDateTime.now());
    List<Message> prevMsgList = new ArrayList<>();
    prevMsgList.add(prevMsg);
    when(messageService.getAllMessages(message.getUserId(), true)).thenReturn(prevMsgList);
    Message msg = messageService.modifyMessage(message, message.getId(), message.getUserId());
    Assertions.assertNotNull(msg);
  }

  @Test
  public void shouldModifyMessageTestFalseCondition() {
    Message message = new Message();
    message.setMessage("Hello, Message");
    message.setUserId("userId123");
    message.setActive(false);
    message.setId("id");
    message.setCreatedAt(LocalDateTime.now());
    message.setUpdatedAt(LocalDateTime.now());
    when(messageRepository.findById(any())).thenReturn(Optional.of(message));
    Message msg = messageService.modifyMessage(message, message.getId(), message.getUserId());
    Assertions.assertNotNull(msg);
  }

  @Test
  public void shouldModifyMessageTestTrueCondition() {
    Message message = new Message();
    message.setMessage("Hello, Message");
    message.setUserId("userId123");
    message.setActive(true);
    message.setId("id");
    message.setCreatedAt(LocalDateTime.now());
    message.setUpdatedAt(LocalDateTime.now());
    when(messageRepository.findById(any())).thenReturn(Optional.of(message));
    Message prevMsg = new Message();
    prevMsg.setMessage("Hello, Prev");
    prevMsg.setUserId("userId123");
    prevMsg.setActive(true);
    prevMsg.setId("id");
    prevMsg.setCreatedAt(LocalDateTime.now());
    prevMsg.setUpdatedAt(LocalDateTime.now());
    List<Message> prevMsgList = new ArrayList<>();
    prevMsgList.add(prevMsg);
    when(messageService.getAllMessages(message.getUserId(), true)).thenReturn(prevMsgList);
    Message msg = messageService.modifyMessage(message, message.getId(), message.getUserId());
    Assertions.assertNotNull(msg);
  }
}
