package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.ShipmentService;
import com.reliablecarriers.Reliable.Carriers.service.ShipmentTrackingService;
import com.reliablecarriers.Reliable.Carriers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final UserService userService;
    private final ShipmentTrackingService shipmentTrackingService;

    @Autowired
    public ShipmentController(ShipmentService shipmentService, UserService userService, ShipmentTrackingService shipmentTrackingService) {
        this.shipmentService = shipmentService;
        this.userService = userService;
        this.shipmentTrackingService = shipmentTrackingService;
    }

    @PostMapping
    public ResponseEntity<Shipment> createShipment(@Valid @RequestBody Shipment shipment) {
        Shipment createdShipment = shipmentService.createShipment(shipment);
        return new ResponseEntity<>(createdShipment, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Shipment> updateShipment(@PathVariable Long id, @Valid @RequestBody Shipment shipment) {
        Shipment updatedShipment = shipmentService.updateShipment(id, shipment);
        return ResponseEntity.ok(updatedShipment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipmentById(@PathVariable Long id) {
        Shipment shipment = shipmentService.getShipmentById(id);
        return ResponseEntity.ok(shipment);
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<Shipment> getShipmentByTrackingNumber(@PathVariable String trackingNumber) {
        Shipment shipment = shipmentService.getShipmentByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(shipment);
    }

    @GetMapping("/tracking/{trackingNumber}/entries")
    public ResponseEntity<List<com.reliablecarriers.Reliable.Carriers.model.ShipmentTracking>> getTrackingEntriesByTrackingNumber(@PathVariable String trackingNumber) {
        Shipment shipment = shipmentService.getShipmentByTrackingNumber(trackingNumber);
        List<com.reliablecarriers.Reliable.Carriers.model.ShipmentTracking> entries = shipmentTrackingService.getTrackingEntriesByShipment(shipment);
        return ResponseEntity.ok(entries);
    }

    @GetMapping
    public ResponseEntity<List<Shipment>> getAllShipments() {
        List<Shipment> shipments = shipmentService.getAllShipments();
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<Shipment>> getShipmentsBySender(@PathVariable Long senderId) {
        User sender = userService.getUserById(senderId);
        List<Shipment> shipments = shipmentService.getShipmentsBySender(sender);
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Shipment>> getShipmentsByDriver(@PathVariable Long driverId) {
        User driver = userService.getUserById(driverId);
        List<Shipment> shipments = shipmentService.getShipmentsByDriver(driver);
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Shipment>> getShipmentsByStatus(@PathVariable ShipmentStatus status) {
        List<Shipment> shipments = shipmentService.getShipmentsByStatus(status);
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Shipment>> getShipmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        List<Shipment> shipments = shipmentService.getShipmentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/delivery-date-range")
    public ResponseEntity<List<Shipment>> getShipmentsByDeliveryDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        List<Shipment> shipments = shipmentService.getShipmentsByDeliveryDateRange(startDate, endDate);
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/pickup-location")
    public ResponseEntity<List<Shipment>> getShipmentsByPickupLocation(
            @RequestParam String city, @RequestParam String state) {
        List<Shipment> shipments = shipmentService.getShipmentsByPickupLocation(city, state);
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/delivery-location")
    public ResponseEntity<List<Shipment>> getShipmentsByDeliveryLocation(
            @RequestParam String city, @RequestParam String state) {
        List<Shipment> shipments = shipmentService.getShipmentsByDeliveryLocation(city, state);
        return ResponseEntity.ok(shipments);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{shipmentId}/assign-driver/{driverId}")
    public ResponseEntity<Shipment> assignDriverToShipment(
            @PathVariable Long shipmentId, @PathVariable Long driverId) {
        Shipment shipment = shipmentService.assignDriverToShipment(shipmentId, driverId);
        return ResponseEntity.ok(shipment);
    }

    @PutMapping("/{shipmentId}/update-status")
    public ResponseEntity<Shipment> updateShipmentStatus(
            @PathVariable Long shipmentId,
            @RequestParam ShipmentStatus status,
            @RequestParam String location,
            @RequestParam(required = false) String notes) {
        Shipment shipment = shipmentService.updateShipmentStatus(shipmentId, status, location, notes);
        return ResponseEntity.ok(shipment);
    }
}