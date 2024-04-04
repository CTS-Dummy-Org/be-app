package com.cognizant.quickstart.messages.service;

import com.cognizant.quickstart.messages.domain.Message;
import java.util.List;

public interface MessageService {
  public Message createMessage(Message message);

  public List<Message> getAllMessages(String userId, Boolean active);

  public Message getMessageById(String id);

  public Message deleteMessage(String id);

  public Message modifyMessage(Message message, String id, String userId);
}
