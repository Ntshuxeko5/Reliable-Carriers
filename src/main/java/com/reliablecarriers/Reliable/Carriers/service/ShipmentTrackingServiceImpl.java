package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.ShipmentTrackingRequest;
import com.reliablecarriers.Reliable.Carriers.dto.ShipmentTrackingResponse;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentTracking;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentTrackingRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;

@Service
public class ShipmentTrackingServiceImpl implements ShipmentTrackingService {

    private final ShipmentTrackingRepository shipmentTrackingRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    @Autowired
    public ShipmentTrackingServiceImpl(ShipmentTrackingRepository shipmentTrackingRepository,
                                       ShipmentRepository shipmentRepository,
                                       UserRepository userRepository) {
        this.shipmentTrackingRepository = shipmentTrackingRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ShipmentTracking createTrackingEntry(ShipmentTracking tracking) {
        // Validate shipment exists
        if (tracking.getShipment() != null && tracking.getShipment().getId() != null) {
            Shipment shipment = shipmentRepository.findById(tracking.getShipment().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Shipment not found with id: " + tracking.getShipment().getId()));
            tracking.setShipment(shipment);
        }

        // Validate user exists
        if (tracking.getUpdatedBy() != null && tracking.getUpdatedBy().getId() != null) {
            User user = userRepository.findById(tracking.getUpdatedBy().getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + tracking.getUpdatedBy().getId()));
            tracking.setUpdatedBy(user);
        }

        // Set creation timestamp if not set
        if (tracking.getCreatedAt() == null) {
            tracking.setCreatedAt(new Date());
        }

        return shipmentTrackingRepository.save(tracking);
    }

    @Override
    public ShipmentTracking getTrackingEntryById(Long id) {
        return shipmentTrackingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tracking entry not found with id: " + id));
    }

    @Override
    public List<ShipmentTracking> getAllTrackingEntries() {
        return shipmentTrackingRepository.findAll();
    }

    @Override
    public List<ShipmentTracking> getTrackingEntriesByShipment(Shipment shipment) {
        return shipmentTrackingRepository.findByShipmentOrderByCreatedAtDesc(shipment);
    }

    @Override
    public List<ShipmentTracking> getTrackingEntriesByShipmentId(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found with id: " + shipmentId));
        return getTrackingEntriesByShipment(shipment);
    }

    @Override
    public List<ShipmentTracking> getTrackingEntriesByStatus(ShipmentStatus status) {
        return shipmentTrackingRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public List<ShipmentTracking> getTrackingEntriesByDateRange(Date startDate, Date endDate) {
        return shipmentTrackingRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }

    @Override
    public List<ShipmentTracking> getTrackingEntriesByLocation(String location) {
        return shipmentTrackingRepository.findByLocationContainingIgnoreCaseOrderByCreatedAtDesc(location);
    }

    @Override
    public List<ShipmentTracking> getTrackingEntriesByUpdatedBy(User user) {
        return shipmentTrackingRepository.findByUpdatedByOrderByCreatedAtDesc(user);
    }

    @Override
    public List<ShipmentTracking> getTrackingEntriesByUpdatedById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return getTrackingEntriesByUpdatedBy(user);
    }

    @Override
    public void deleteTrackingEntry(Long id) {
        ShipmentTracking tracking = getTrackingEntryById(id);
        shipmentTrackingRepository.delete(tracking);
    }

    @Override
    public ShipmentTrackingResponse createTrackingEntryFromRequest(ShipmentTrackingRequest request) {
        // Create ShipmentTracking entity from request
        ShipmentTracking tracking = new ShipmentTracking();
        
        // Set shipment
        if (request.getShipmentId() != null) {
            Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Shipment not found with id: " + request.getShipmentId()));
            tracking.setShipment(shipment);
        }
        
        // Set other fields
        tracking.setStatus(request.getStatus());
        tracking.setLocation(request.getLocation());
        tracking.setNotes(request.getNotes());
        
        // Set updated by user
        if (request.getUpdatedById() != null) {
            User user = userRepository.findById(request.getUpdatedById())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getUpdatedById()));
            tracking.setUpdatedBy(user);
        }
        
        // Set timestamp
        if (request.getCreatedAt() != null) {
            tracking.setCreatedAt(request.getCreatedAt());
        } else {
            tracking.setCreatedAt(new Date());
        }
        
        // Save the tracking entry
        ShipmentTracking savedTracking = shipmentTrackingRepository.save(tracking);
        
        // Return response DTO
        return new ShipmentTrackingResponse(savedTracking);
    }
}