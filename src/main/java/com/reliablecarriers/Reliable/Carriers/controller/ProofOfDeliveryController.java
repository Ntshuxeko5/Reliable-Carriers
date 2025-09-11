package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.ProofOfDelivery;
import com.reliablecarriers.Reliable.Carriers.service.ProofOfDeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pod")
@CrossOrigin(origins = "*")
public class ProofOfDeliveryController {

    @Autowired
    private ProofOfDeliveryService proofOfDeliveryService;

    // Create proof of delivery
    @PostMapping("/create")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<ProofOfDelivery> createProofOfDelivery(
            @RequestParam Long shipmentId,
            @RequestParam Long driverId,
            @RequestParam String deliveryLocation) {
        try {
            ProofOfDelivery pod = proofOfDeliveryService.createProofOfDelivery(shipmentId, driverId, deliveryLocation);
            return ResponseEntity.ok(pod);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Add signature to proof of delivery
    @PutMapping("/{id}/signature")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<ProofOfDelivery> addSignature(
            @PathVariable Long id,
            @RequestParam String signatureData) {
        try {
            ProofOfDelivery pod = proofOfDeliveryService.addSignature(id, signatureData);
            return ResponseEntity.ok(pod);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Upload delivery photo
    @PostMapping("/{id}/upload-photo")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<ProofOfDelivery> uploadDeliveryPhoto(
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile photo) {
        try {
            ProofOfDelivery pod = proofOfDeliveryService.uploadDeliveryPhoto(id, photo);
            return ResponseEntity.ok(pod);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Upload package photo
    @PostMapping("/{id}/upload-package-photo")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<ProofOfDelivery> uploadPackagePhoto(
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile photo) {
        try {
            ProofOfDelivery pod = proofOfDeliveryService.uploadPackagePhoto(id, photo);
            return ResponseEntity.ok(pod);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Complete delivery
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<ProofOfDelivery> completeDelivery(
            @PathVariable Long id,
            @RequestParam String recipientName,
            @RequestParam String recipientPhone,
            @RequestParam(required = false) String recipientIdNumber,
            @RequestParam(required = false) String deliveryNotes,
            @RequestParam(required = false) String deliveryMethod) {
        try {
            ProofOfDelivery pod = proofOfDeliveryService.completeDelivery(
                    id, recipientName, recipientPhone, recipientIdNumber, deliveryNotes, deliveryMethod);
            return ResponseEntity.ok(pod);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Mark delivery as failed
    @PutMapping("/{id}/failed")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<ProofOfDelivery> markDeliveryFailed(
            @PathVariable Long id,
            @RequestParam String failureReason) {
        try {
            ProofOfDelivery pod = proofOfDeliveryService.markDeliveryFailed(id, failureReason);
            return ResponseEntity.ok(pod);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get proof of delivery by shipment ID
    @GetMapping("/shipment/{shipmentId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<ProofOfDelivery> getProofOfDeliveryByShipment(@PathVariable Long shipmentId) {
        try {
            ProofOfDelivery pod = proofOfDeliveryService.getProofOfDeliveryByShipment(shipmentId);
            return ResponseEntity.ok(pod);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get proof of delivery by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<ProofOfDelivery> getProofOfDeliveryById(@PathVariable Long id) {
        try {
            ProofOfDelivery pod = proofOfDeliveryService.getProofOfDeliveryById(id);
            return ResponseEntity.ok(pod);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get proof of deliveries by driver
    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<ProofOfDelivery>> getProofOfDeliveriesByDriver(@PathVariable Long driverId) {
        try {
            List<ProofOfDelivery> pods = proofOfDeliveryService.getProofOfDeliveriesByDriver(driverId);
            return ResponseEntity.ok(pods);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get proof of deliveries by date range
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<ProofOfDelivery>> getProofOfDeliveriesByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            List<ProofOfDelivery> pods = proofOfDeliveryService.getProofOfDeliveriesByDateRange(startDate, endDate);
            return ResponseEntity.ok(pods);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get proof of deliveries by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<ProofOfDelivery>> getProofOfDeliveriesByStatus(@PathVariable String status) {
        try {
            List<ProofOfDelivery> pods = proofOfDeliveryService.getProofOfDeliveriesByStatus(status);
            return ResponseEntity.ok(pods);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get delivery statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<Map<String, Object>> getDeliveryStatistics() {
        try {
            Map<String, Object> stats = proofOfDeliveryService.getDeliveryStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Validate delivery requirements
    @GetMapping("/validate/{shipmentId}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<Map<String, Object>> validateDeliveryRequirements(@PathVariable Long shipmentId) {
        try {
            Map<String, Object> validation = proofOfDeliveryService.validateDeliveryRequirements(shipmentId);
            return ResponseEntity.ok(validation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Generate delivery report
    @GetMapping("/{id}/report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<byte[]> generateDeliveryReport(@PathVariable Long id) {
        try {
            byte[] report = proofOfDeliveryService.generateDeliveryReport(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "delivery-report-" + id + ".pdf");
            return new ResponseEntity<>(report, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Send delivery confirmation
    @PostMapping("/{id}/send-confirmation")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<Void> sendDeliveryConfirmation(@PathVariable Long id) {
        try {
            proofOfDeliveryService.sendDeliveryConfirmation(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get delivery photo URL
    @GetMapping("/{id}/delivery-photo")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<String> getDeliveryPhotoUrl(@PathVariable Long id) {
        try {
            String photoUrl = proofOfDeliveryService.getDeliveryPhotoUrl(id);
            return ResponseEntity.ok(photoUrl);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get package photo URL
    @GetMapping("/{id}/package-photo")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<String> getPackagePhotoUrl(@PathVariable Long id) {
        try {
            String photoUrl = proofOfDeliveryService.getPackagePhotoUrl(id);
            return ResponseEntity.ok(photoUrl);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete proof of delivery
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProofOfDelivery(@PathVariable Long id) {
        try {
            proofOfDeliveryService.deleteProofOfDelivery(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Update delivery notes
    @PutMapping("/{id}/notes")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<ProofOfDelivery> updateDeliveryNotes(
            @PathVariable Long id,
            @RequestParam String notes) {
        try {
            ProofOfDelivery pod = proofOfDeliveryService.updateDeliveryNotes(id, notes);
            return ResponseEntity.ok(pod);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get delivery history
    @GetMapping("/history/{shipmentId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('DRIVER') or hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<ProofOfDelivery>> getDeliveryHistory(@PathVariable Long shipmentId) {
        try {
            List<ProofOfDelivery> history = proofOfDeliveryService.getDeliveryHistory(shipmentId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
