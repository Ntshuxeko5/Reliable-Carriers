package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    
    List<ContactMessage> findByStatusOrderByCreatedAtDesc(ContactMessage.MessageStatus status);
    
    List<ContactMessage> findAllByOrderByCreatedAtDesc();
    
    List<ContactMessage> findByEmailOrderByCreatedAtDesc(String email);
    
    @Query("SELECT COUNT(c) FROM ContactMessage c WHERE c.status = 'NEW'")
    long countUnreadMessages();
    
    @Query("SELECT c FROM ContactMessage c WHERE c.createdAt >= :since ORDER BY c.createdAt DESC")
    List<ContactMessage> findRecentMessages(Date since);
}

