package com.cognizant.quickstart.messages.data;

import com.cognizant.quickstart.messages.domain.Message;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
  public List<Message> findByUserId(String userId);

  public List<Message> findByUserIdAndActive(String userId, Boolean active);

}
