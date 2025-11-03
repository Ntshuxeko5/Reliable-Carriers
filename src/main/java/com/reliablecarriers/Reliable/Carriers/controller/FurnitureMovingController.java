package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.FurnitureMovingQuoteRequest;
import com.reliablecarriers.Reliable.Carriers.service.FurnitureMovingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/furniture-moving")
public class FurnitureMovingController {

    @Autowired
    private FurnitureMovingService furnitureMovingService;

    /**
     * Generate furniture moving quote
     */
    @PostMapping("/quote")
    public ResponseEntity<Map<String, Object>> generateQuote(@RequestBody FurnitureMovingQuoteRequest request) {
        try {
            Map<String, Object> result = furnitureMovingService.generateQuote(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error generating quote: " + e.getMessage()
            ));
        }
    }

    /**
     * Get quote details by ID
     */
    @GetMapping("/quote/{quoteId}")
    public ResponseEntity<Map<String, Object>> getQuote(@PathVariable String quoteId) {
        try {
            Map<String, Object> quote = furnitureMovingService.getQuoteById(quoteId);
            if (quote != null) {
                return ResponseEntity.ok(quote);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error retrieving quote: " + e.getMessage()
            ));
        }
    }
}

