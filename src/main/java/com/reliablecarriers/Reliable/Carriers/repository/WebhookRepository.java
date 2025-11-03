package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.Webhook;
import com.reliablecarriers.Reliable.Carriers.model.WebhookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    
    List<Webhook> findByUser(User user);
    
    List<Webhook> findByUserAndStatus(User user, WebhookStatus status);
    
    List<Webhook> findByStatus(WebhookStatus status);
}





