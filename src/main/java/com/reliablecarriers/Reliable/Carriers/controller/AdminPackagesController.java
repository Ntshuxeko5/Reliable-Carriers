package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/packages")
public class AdminPackagesController {

    @Autowired
    private ShipmentRepository shipmentRepository;

    /**
     * Get all packages with detailed information
     */
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllPackages() {
        try {
            List<Shipment> allPackages = shipmentRepository.findAll();
            
            List<Map<String, Object>> packages = allPackages.stream()
                .map(pkg -> {
                    Map<String, Object> packageMap = new HashMap<>();
                    packageMap.put("id", pkg.getId());
                    packageMap.put("trackingNumber", pkg.getTrackingNumber());
                    packageMap.put("senderName", pkg.getSender().getFirstName() + " " + pkg.getSender().getLastName());
                    packageMap.put("senderEmail", pkg.getSender().getEmail());
                    packageMap.put("senderPhone", pkg.getSender().getPhone());
                    packageMap.put("recipientName", pkg.getRecipientName());
                    packageMap.put("recipientEmail", pkg.getRecipientEmail());
                    packageMap.put("recipientPhone", pkg.getRecipientPhone());
                    packageMap.put("pickupAddress", pkg.getPickupAddress());
                    packageMap.put("pickupCity", pkg.getPickupCity());
                    packageMap.put("pickupState", pkg.getPickupState());
                    packageMap.put("pickupZipCode", pkg.getPickupZipCode());
                    packageMap.put("deliveryAddress", pkg.getDeliveryAddress());
                    packageMap.put("deliveryCity", pkg.getDeliveryCity());
                    packageMap.put("deliveryState", pkg.getDeliveryState());
                    packageMap.put("deliveryZipCode", pkg.getDeliveryZipCode());
                    packageMap.put("weight", pkg.getWeight());
                    packageMap.put("dimensions", pkg.getDimensions());
                    packageMap.put("description", pkg.getDescription());
                    packageMap.put("shippingCost", pkg.getShippingCost());
                    packageMap.put("serviceType", pkg.getServiceType());
                    packageMap.put("status", pkg.getStatus());
                    packageMap.put("assignedDriverId", pkg.getAssignedDriver() != null ? pkg.getAssignedDriver().getId() : null);
                    packageMap.put("assignedDriverName", pkg.getAssignedDriver() != null ? 
                        pkg.getAssignedDriver().getFirstName() + " " + pkg.getAssignedDriver().getLastName() : null);
                    packageMap.put("assignedDriverPhone", pkg.getAssignedDriver() != null ? pkg.getAssignedDriver().getPhone() : null);
                    packageMap.put("estimatedDeliveryDate", pkg.getEstimatedDeliveryDate());
                    packageMap.put("actualDeliveryDate", pkg.getActualDeliveryDate());
                    packageMap.put("createdAt", pkg.getCreatedAt());
                    packageMap.put("updatedAt", pkg.getUpdatedAt());
                    
                    // Add location coordinates (simplified for demo)
                    packageMap.put("pickupLatitude", -26.2041 + (Math.random() - 0.5) * 0.1);
                    packageMap.put("pickupLongitude", 28.0473 + (Math.random() - 0.5) * 0.1);
                    packageMap.put("deliveryLatitude", -26.2041 + (Math.random() - 0.5) * 0.1);
                    packageMap.put("deliveryLongitude", 28.0473 + (Math.random() - 0.5) * 0.1);
                    
                    return packageMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get packages by status
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<Map<String, Object>>> getPackagesByStatus(@PathVariable String status) {
        try {
            ShipmentStatus shipmentStatus = ShipmentStatus.valueOf(status.toUpperCase());
            List<Shipment> packages = shipmentRepository.findByStatus(shipmentStatus);
            
            List<Map<String, Object>> packageList = packages.stream()
                .map(pkg -> {
                    Map<String, Object> packageMap = new HashMap<>();
                    packageMap.put("id", pkg.getId());
                    packageMap.put("trackingNumber", pkg.getTrackingNumber());
                    packageMap.put("recipientName", pkg.getRecipientName());
                    packageMap.put("pickupAddress", pkg.getPickupAddress());
                    packageMap.put("deliveryAddress", pkg.getDeliveryAddress());
                    packageMap.put("status", pkg.getStatus());
                    packageMap.put("assignedDriverName", pkg.getAssignedDriver() != null ? 
                        pkg.getAssignedDriver().getFirstName() + " " + pkg.getAssignedDriver().getLastName() : null);
                    packageMap.put("createdAt", pkg.getCreatedAt());
                    return packageMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(packageList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get package statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPackageStatistics() {
        try {
            List<Shipment> allPackages = shipmentRepository.findAll();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", allPackages.size());
            stats.put("pending", allPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.PENDING).count());
            stats.put("assigned", allPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.ASSIGNED).count());
            stats.put("pickedUp", allPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.PICKED_UP).count());
            stats.put("inTransit", allPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.IN_TRANSIT).count());
            stats.put("outForDelivery", allPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY).count());
            stats.put("delivered", allPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.DELIVERED).count());
            stats.put("failed", allPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.FAILED_DELIVERY).count());
            stats.put("cancelled", allPackages.stream().filter(p -> p.getStatus() == ShipmentStatus.CANCELLED).count());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
