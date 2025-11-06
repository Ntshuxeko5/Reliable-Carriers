package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageRequest;
import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageResponse;
import com.reliablecarriers.Reliable.Carriers.dto.QuoteResponse;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.service.CustomerPackageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
public class CustomerPackageController {

    private final CustomerPackageService customerPackageService;

    public CustomerPackageController(CustomerPackageService customerPackageService) {
        this.customerPackageService = customerPackageService;
    }

    // Quote Management
    @PostMapping("/quote")
    public ResponseEntity<Map<String, Object>> createQuote(@Valid @RequestBody CustomerPackageRequest request) {
        try {
            QuoteResponse quote = customerPackageService.createQuote(request);
            return new ResponseEntity<>(Map.of("success", true, "data", quote), HttpStatus.CREATED);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error creating quote: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to create quote: " + e.getMessage(),
                "details", e.getClass().getSimpleName()
            ));
        }
    }

    @PostMapping("/quote/{quoteId}/create-shipment")
    public ResponseEntity<Map<String, Object>> createShipmentFromQuote(
            @PathVariable String quoteId,
            @Valid @RequestBody CustomerPackageRequest request) {
        try {
            Shipment shipment = customerPackageService.createShipmentFromQuote(quoteId, request);
            return ResponseEntity.ok(Map.of(
                "message", "Shipment created successfully",
                "trackingNumber", shipment.getTrackingNumber(),
                "shipmentId", shipment.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Package Tracking (No Account Required)
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<CustomerPackageResponse> trackPackage(@PathVariable String trackingNumber) {
        try {
            if (!customerPackageService.isValidTrackingNumber(trackingNumber)) {
                return ResponseEntity.badRequest().build();
            }
            
            CustomerPackageResponse packageInfo = customerPackageService.getPackageByTrackingNumber(trackingNumber);
            return ResponseEntity.ok(packageInfo);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Public tracking endpoint for guests (no authentication required)
    @GetMapping("/public/track/{trackingNumber}")
    public ResponseEntity<CustomerPackageResponse> trackPackagePublic(@PathVariable String trackingNumber) {
        try {
            if (!customerPackageService.isValidTrackingNumber(trackingNumber)) {
                return ResponseEntity.badRequest().build();
            }
            
            CustomerPackageResponse packageInfo = customerPackageService.getPackageByTrackingNumber(trackingNumber);
            return ResponseEntity.ok(packageInfo);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/track/{trackingNumber}/estimated-delivery")
    public ResponseEntity<Map<String, String>> getEstimatedDeliveryDate(@PathVariable String trackingNumber) {
        try {
            String estimatedDate = customerPackageService.getEstimatedDeliveryDate(trackingNumber);
            return ResponseEntity.ok(Map.of("estimatedDeliveryDate", estimatedDate));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Package Management by Email (No Account Required)
    @GetMapping("/packages/email/{email}")
    public ResponseEntity<List<CustomerPackageResponse>> getPackagesByEmail(@PathVariable String email) {
        try {
            List<CustomerPackageResponse> packages = customerPackageService.getPackagesByEmail(email);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of()); // Return empty list if no packages found
        }
    }

    @GetMapping("/packages/phone/{phone}")
    public ResponseEntity<List<CustomerPackageResponse>> getPackagesByPhone(@PathVariable String phone) {
        try {
            List<CustomerPackageResponse> packages = customerPackageService.getPackagesByPhone(phone);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of()); // Return empty list if no packages found
        }
    }

    // Package Status Filtering
    @GetMapping("/packages/email/{email}/status/{status}")
    public ResponseEntity<List<CustomerPackageResponse>> getPackagesByStatus(
            @PathVariable String email,
            @PathVariable String status) {
        try {
            List<CustomerPackageResponse> packages = customerPackageService.getPackagesByStatus(email, status);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/packages/email/{email}/delivered")
    public ResponseEntity<List<CustomerPackageResponse>> getDeliveredPackages(@PathVariable String email) {
        try {
            List<CustomerPackageResponse> packages = customerPackageService.getDeliveredPackages(email);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/packages/email/{email}/current")
    public ResponseEntity<List<CustomerPackageResponse>> getCurrentPackages(@PathVariable String email) {
        try {
            List<CustomerPackageResponse> packages = customerPackageService.getCurrentPackages(email);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/packages/email/{email}/pending")
    public ResponseEntity<List<CustomerPackageResponse>> getPendingPackages(@PathVariable String email) {
        try {
            List<CustomerPackageResponse> packages = customerPackageService.getPendingPackages(email);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    // Package History and Search
    @GetMapping("/packages/email/{email}/history")
    public ResponseEntity<List<CustomerPackageResponse>> getPackageHistory(
            @PathVariable String email,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<CustomerPackageResponse> packages = customerPackageService.getPackageHistory(email, limit);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/packages/email/{email}/search")
    public ResponseEntity<List<CustomerPackageResponse>> searchPackages(
            @PathVariable String email,
            @RequestParam String searchTerm) {
        try {
            List<CustomerPackageResponse> packages = customerPackageService.searchPackages(email, searchTerm);
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    // Package Statistics
    @GetMapping("/packages/email/{email}/statistics")
    public ResponseEntity<CustomerPackageService.PackageStatistics> getPackageStatistics(@PathVariable String email) {
        try {
            CustomerPackageService.PackageStatistics stats = customerPackageService.getPackageStatistics(email);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.ok(new CustomerPackageService.PackageStatistics());
        }
    }

    // Package Actions
    @PutMapping("/packages/{trackingNumber}/cancel")
    public ResponseEntity<Map<String, Object>> cancelPackage(
            @PathVariable String trackingNumber,
            @RequestParam String email) {
        try {
            boolean cancelled = customerPackageService.cancelPackage(trackingNumber, email);
            if (cancelled) {
                return ResponseEntity.ok(Map.of("message", "Package cancelled successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Package cannot be cancelled"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/packages/{trackingNumber}/pickup-request")
    public ResponseEntity<Map<String, Object>> requestPickup(
            @PathVariable String trackingNumber,
            @RequestParam String email,
            @RequestParam String preferredDate,
            @RequestParam(required = false) String notes) {
        try {
            customerPackageService.requestPickup(trackingNumber, email, preferredDate, notes);
            return ResponseEntity.ok(Map.of("message", "Pickup request submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/packages/{trackingNumber}/tracking-preferences")
    public ResponseEntity<Map<String, Object>> updateTrackingPreferences(
            @PathVariable String trackingNumber,
            @RequestParam boolean emailNotifications,
            @RequestParam boolean smsNotifications) {
        try {
            customerPackageService.updateTrackingPreferences(trackingNumber, emailNotifications, smsNotifications);
            return ResponseEntity.ok(Map.of("message", "Tracking preferences updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Package Eligibility and Options
    @GetMapping("/packages/{trackingNumber}/eligible-for-pickup")
    public ResponseEntity<Map<String, Boolean>> isEligibleForPickup(@PathVariable String trackingNumber) {
        try {
            boolean eligible = customerPackageService.isEligibleForPickup(trackingNumber);
            return ResponseEntity.ok(Map.of("eligible", eligible));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/packages/{trackingNumber}/insurance-options")
    public ResponseEntity<List<CustomerPackageService.InsuranceOption>> getInsuranceOptions(@PathVariable String trackingNumber) {
        try {
            List<CustomerPackageService.InsuranceOption> options = customerPackageService.getInsuranceOptions(trackingNumber);
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/packages/{trackingNumber}/add-insurance")
    public ResponseEntity<Map<String, Object>> addInsurance(
            @PathVariable String trackingNumber,
            @RequestParam String insuranceType,
            @RequestParam BigDecimal amount) {
        try {
            boolean added = customerPackageService.addInsurance(trackingNumber, insuranceType, amount);
            if (added) {
                return ResponseEntity.ok(Map.of("message", "Insurance added successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Insurance cannot be added"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Store/Business Operations
    @PostMapping("/store/multiple-packages")
    public ResponseEntity<Map<String, Object>> createMultiplePackages(
            @RequestBody List<CustomerPackageRequest> requests,
            @RequestParam String businessName) {
        try {
            List<Shipment> shipments = customerPackageService.createMultiplePackages(requests, businessName);
            return ResponseEntity.ok(Map.of(
                "message", "Multiple packages created successfully",
                "count", shipments.size(),
                "trackingNumbers", shipments.stream().map(Shipment::getTrackingNumber).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Validation
    @GetMapping("/validate-tracking/{trackingNumber}")
    public ResponseEntity<Map<String, Boolean>> validateTrackingNumber(@PathVariable String trackingNumber) {
        boolean isValid = customerPackageService.isValidTrackingNumber(trackingNumber);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    // Saved Quotes Management
    @GetMapping("/quotes")
    public ResponseEntity<List<Map<String, Object>>> getSavedQuotes(
            org.springframework.security.core.Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(List.of());
            }
            
            String email = authentication.getName();
            List<com.reliablecarriers.Reliable.Carriers.model.Quote> quotes = 
                customerPackageService.getSavedQuotes(email);
            
            List<Map<String, Object>> quoteList = quotes.stream().map(quote -> {
                Map<String, Object> quoteMap = new java.util.HashMap<>();
                quoteMap.put("quoteId", quote.getQuoteId());
                quoteMap.put("pickupAddress", quote.getPickupAddress());
                quoteMap.put("deliveryAddress", quote.getDeliveryAddress());
                quoteMap.put("weight", quote.getWeight());
                quoteMap.put("dimensions", quote.getDimensions());
                quoteMap.put("totalCost", quote.getTotalCost());
                quoteMap.put("serviceType", quote.getServiceType());
                quoteMap.put("estimatedDeliveryTime", quote.getEstimatedDeliveryTime());
                quoteMap.put("estimatedDeliveryDate", quote.getEstimatedDeliveryDate());
                quoteMap.put("expiryDate", quote.getExpiryDate());
                quoteMap.put("createdAt", quote.getCreatedAt());
                quoteMap.put("isActive", quote.getIsActive());
                return quoteMap;
            }).collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(quoteList);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "Customer Package Service is running"));
    }
}
