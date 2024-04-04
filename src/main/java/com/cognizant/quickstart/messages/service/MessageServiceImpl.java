package com.cognizant.quickstart.messages.service;

import com.cognizant.quickstart.messages.data.MessageRepository;
import com.cognizant.quickstart.messages.domain.Message;
import com.cognizant.quickstart.messages.exceptions.MessageNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

  @Autowired
  private MessageRepository messageRepository;

  @Override
  public Message createMessage(Message message) {
    messageRepository.save(message);
    log.info("Message '{}' added to DB.", message.getMessage());
    return message;
  }

  @Override
  public List<Message> getAllMessages(String userId, Boolean active) {
    if (active != null) {
      return messageRepository.findByUserIdAndActive(userId, active);
    } else {
      return messageRepository.findByUserId(userId);
    }
  }

  @Override
  public Message deleteMessage(String id) {
    Message deleteMessage = getMessageById(id);
    messageRepository.delete(deleteMessage);
    log.info("Deleted Message '{}' from DB.", deleteMessage.getMessage());
    return deleteMessage;
  }

  public Message getMessageById(String id) {
    return messageRepository.findById(id)
        .orElseThrow(() -> new MessageNotFoundException("Message does not exist with id :" + id));
  }

  @Override
  public Message modifyMessage(Message message, String id, String userId) {
    if (message.isActive()) {
      List<Message> prevActiveMsg = getAllMessages(userId, true);
      for (Message m : prevActiveMsg) {
        if (m.isActive()) {
          m.setActive(false);
          m.setUpdatedAt(LocalDateTime.now());
          messageRepository.save(m);
        }
      }
      Message msg = getMessageById(id);
      msg.setActive(message.isActive());
      msg.setUpdatedAt(LocalDateTime.now());
      messageRepository.save(msg);
      log.info("Message '{}' is updated as active message.", msg.getMessage());
      return msg;
    } else {
      Message msg = getMessageById(id);
      msg.setMessage(message.getMessage());
      msg.setUpdatedAt(LocalDateTime.now());
      messageRepository.save(msg);
      log.info("Message '{}' is updated in DB.", msg.getMessage());
      return msg;
    }
  }
}
