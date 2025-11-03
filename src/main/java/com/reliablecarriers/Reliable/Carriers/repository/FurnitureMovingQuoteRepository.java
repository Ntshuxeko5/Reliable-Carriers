package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.FurnitureMovingQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FurnitureMovingQuoteRepository extends JpaRepository<FurnitureMovingQuote, Long> {
    
    Optional<FurnitureMovingQuote> findByQuoteId(String quoteId);
    
    List<FurnitureMovingQuote> findByCustomerEmail(String customerEmail);
    
    List<FurnitureMovingQuote> findByCustomerPhone(String customerPhone);
    
    List<FurnitureMovingQuote> findByStatus(FurnitureMovingQuote.QuoteStatus status);
    
    List<FurnitureMovingQuote> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT f FROM FurnitureMovingQuote f WHERE f.customerEmail = :email OR f.customerPhone = :phone")
    List<FurnitureMovingQuote> findByCustomerEmailOrPhone(@Param("email") String email, @Param("phone") String phone);
    
    @Query("SELECT COUNT(f) FROM FurnitureMovingQuote f WHERE f.createdAt >= :startDate")
    Long countQuotesFromDate(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT f FROM FurnitureMovingQuote f WHERE f.movingDate = :movingDate ORDER BY f.createdAt DESC")
    List<FurnitureMovingQuote> findByMovingDateOrderByCreatedAtDesc(@Param("movingDate") String movingDate);
}

