package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.ShipmentService;
import com.reliablecarriers.Reliable.Carriers.service.ShipmentTrackingService;
import com.reliablecarriers.Reliable.Carriers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<Shipment> assignDriverToShipment(
            @PathVariable Long shipmentId, @PathVariable Long driverId) {
        Shipment shipment = shipmentService.assignDriverToShipment(shipmentId, driverId);
        return ResponseEntity.ok(shipment);
    }

    @PutMapping("/{shipmentId}/update-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER') or hasRole('DRIVER')")
    public ResponseEntity<Shipment> updateShipmentStatus(
            @PathVariable Long shipmentId,
            @RequestParam ShipmentStatus status,
            @RequestParam String location,
            @RequestParam(required = false) String notes) {
        Shipment shipment = shipmentService.updateShipmentStatus(shipmentId, status, location, notes);
        return ResponseEntity.ok(shipment);
    }

    /**
     * Update package information - allows admin, tracking manager, and driver to update package details
     */
    @PutMapping("/{shipmentId}/update-package-info")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER') or hasRole('DRIVER')")
    public ResponseEntity<Shipment> updatePackageInfo(
            @PathVariable Long shipmentId,
            @RequestBody Map<String, Object> updates) {
        try {
            Shipment shipment = shipmentService.getShipmentById(shipmentId);
            
            // Update fields if provided
            if (updates.containsKey("status")) {
                String statusStr = updates.get("status").toString();
                ShipmentStatus status = ShipmentStatus.valueOf(statusStr.toUpperCase());
                shipment.setStatus(status);
                
                // If delivered, set actual delivery date
                if (status == ShipmentStatus.DELIVERED) {
                    shipment.setActualDeliveryDate(new Date());
                }
            }
            
            if (updates.containsKey("location")) {
                String location = updates.get("location").toString();
                // Create tracking entry for location update
                shipmentService.updateShipmentStatus(shipmentId, shipment.getStatus(), location, "Location updated");
            }
            
            if (updates.containsKey("notes")) {
                String notes = updates.get("notes").toString();
                // Create tracking entry for notes update
                shipmentService.updateShipmentStatus(shipmentId, shipment.getStatus(), "System", notes);
            }
            
            if (updates.containsKey("estimatedDeliveryDate")) {
                String dateStr = updates.get("estimatedDeliveryDate").toString();
                try {
                    Date estimatedDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                    shipment.setEstimatedDeliveryDate(estimatedDate);
                } catch (Exception e) {
                    // Invalid date format, ignore
                }
            }
            
            if (updates.containsKey("serviceType")) {
                String serviceTypeStr = updates.get("serviceType").toString();
                ServiceType serviceType = ServiceType.valueOf(serviceTypeStr.toUpperCase());
                shipment.setServiceType(serviceType);
            }
            
            // Save updated shipment
            Shipment updatedShipment = shipmentService.updateShipment(shipmentId, shipment);
            
            return ResponseEntity.ok(updatedShipment);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all shipments for admin/tracking manager management
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
    public ResponseEntity<List<Shipment>> getAllShipmentsForAdmin() {
        List<Shipment> shipments = shipmentService.getAllShipments();
        return ResponseEntity.ok(shipments);
    }

    /**
     * Get shipments assigned to a specific driver (for admin/tracking manager)
     */
    @GetMapping("/driver/{driverId}/assigned")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER') or hasRole('DRIVER')")
    public ResponseEntity<List<Shipment>> getShipmentsAssignedToDriver(@PathVariable Long driverId) {
        List<Shipment> shipments = shipmentService.getShipmentsByDriverId(driverId);
        return ResponseEntity.ok(shipments);
    }
}