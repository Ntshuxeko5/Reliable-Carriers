package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.ShipmentTrackingRequest;
import com.reliablecarriers.Reliable.Carriers.dto.ShipmentTrackingResponse;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentTracking;
import com.reliablecarriers.Reliable.Carriers.service.ShipmentTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tracking")
public class ShipmentTrackingController {

    private final ShipmentTrackingService shipmentTrackingService;

    @Autowired
    public ShipmentTrackingController(ShipmentTrackingService shipmentTrackingService) {
        this.shipmentTrackingService = shipmentTrackingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('DRIVER')")
    public ResponseEntity<ShipmentTrackingResponse> createTrackingEntry(@Valid @RequestBody ShipmentTrackingRequest request) {
        ShipmentTrackingResponse response = shipmentTrackingService.createTrackingEntryFromRequest(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('DRIVER')")
    public ResponseEntity<ShipmentTrackingResponse> getTrackingEntryById(@PathVariable Long id) {
        ShipmentTracking tracking = shipmentTrackingService.getTrackingEntryById(id);
        ShipmentTrackingResponse response = new ShipmentTrackingResponse(tracking);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<ShipmentTrackingResponse>> getAllTrackingEntries() {
        List<ShipmentTracking> trackingEntries = shipmentTrackingService.getAllTrackingEntries();
        List<ShipmentTrackingResponse> responses = trackingEntries.stream()
                .map(ShipmentTrackingResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/shipment/{shipmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('DRIVER')")
    public ResponseEntity<List<ShipmentTrackingResponse>> getTrackingEntriesByShipmentId(@PathVariable Long shipmentId) {
        List<ShipmentTracking> trackingEntries = shipmentTrackingService.getTrackingEntriesByShipmentId(shipmentId);
        List<ShipmentTrackingResponse> responses = trackingEntries.stream()
                .map(ShipmentTrackingResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<ShipmentTrackingResponse>> getTrackingEntriesByStatus(@PathVariable ShipmentStatus status) {
        List<ShipmentTracking> trackingEntries = shipmentTrackingService.getTrackingEntriesByStatus(status);
        List<ShipmentTrackingResponse> responses = trackingEntries.stream()
                .map(ShipmentTrackingResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<ShipmentTrackingResponse>> getTrackingEntriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        List<ShipmentTracking> trackingEntries = shipmentTrackingService.getTrackingEntriesByDateRange(startDate, endDate);
        List<ShipmentTrackingResponse> responses = trackingEntries.stream()
                .map(ShipmentTrackingResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/location/{location}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<ShipmentTrackingResponse>> getTrackingEntriesByLocation(@PathVariable String location) {
        List<ShipmentTracking> trackingEntries = shipmentTrackingService.getTrackingEntriesByLocation(location);
        List<ShipmentTrackingResponse> responses = trackingEntries.stream()
                .map(ShipmentTrackingResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/updated-by/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<ShipmentTrackingResponse>> getTrackingEntriesByUpdatedById(@PathVariable Long userId) {
        List<ShipmentTracking> trackingEntries = shipmentTrackingService.getTrackingEntriesByUpdatedById(userId);
        List<ShipmentTrackingResponse> responses = trackingEntries.stream()
                .map(ShipmentTrackingResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTrackingEntry(@PathVariable Long id) {
        shipmentTrackingService.deleteTrackingEntry(id);
        return ResponseEntity.noContent().build();
    }
}