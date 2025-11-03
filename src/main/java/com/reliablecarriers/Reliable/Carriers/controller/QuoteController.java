package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.QuoteRequest;
import com.reliablecarriers.Reliable.Carriers.dto.QuoteResponse;
import com.reliablecarriers.Reliable.Carriers.service.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/quote")
public class QuoteController {

    @Autowired
    private QuoteService quoteService;

    @PostMapping("/calculate")
    public ResponseEntity<QuoteResponse> calculateQuote(@RequestBody QuoteRequest request) {
        try {
            QuoteResponse response = quoteService.calculateQuote(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getServiceOptions() {
        try {
            Map<String, Object> services = quoteService.getServiceOptions();
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/validate-dimensions")
    public ResponseEntity<Map<String, Object>> validateDimensions(@RequestBody Map<String, Object> dimensions) {
        try {
            Map<String, Object> result = quoteService.validateDimensions(dimensions);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


