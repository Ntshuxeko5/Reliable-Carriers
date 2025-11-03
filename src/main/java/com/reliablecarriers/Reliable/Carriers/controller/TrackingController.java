package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.TrackingResponse;
import com.reliablecarriers.Reliable.Carriers.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    @Autowired
    private TrackingService trackingService;

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<TrackingResponse> getTrackingInfo(@PathVariable String trackingNumber) {
        try {
            TrackingResponse response = trackingService.getTrackingInfo(trackingNumber);
            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{trackingNumber}/status")
    public ResponseEntity<Map<String, Object>> getTrackingStatus(@PathVariable String trackingNumber) {
        try {
            Map<String, Object> status = trackingService.getTrackingStatus(trackingNumber);
            if (status != null) {
                return ResponseEntity.ok(status);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{trackingNumber}/timeline")
    public ResponseEntity<Map<String, Object>> getTrackingTimeline(@PathVariable String trackingNumber) {
        try {
            Map<String, Object> timeline = trackingService.getTrackingTimeline(trackingNumber);
            if (timeline != null) {
                return ResponseEntity.ok(timeline);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{trackingNumber}/driver")
    public ResponseEntity<Map<String, Object>> getDriverInfo(@PathVariable String trackingNumber) {
        try {
            Map<String, Object> driverInfo = trackingService.getDriverInfo(trackingNumber);
            if (driverInfo != null) {
                return ResponseEntity.ok(driverInfo);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}