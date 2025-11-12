package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasAnyRole('ADMIN','TRACKING_MANAGER')")
@CrossOrigin(origins = "*")
public class AdminAnalyticsController {

    @Autowired
    private BookingRepository bookingRepository;

    // Note: These repositories are kept for potential future use in analytics
    // @Autowired
    // private UserRepository userRepository;
    // @Autowired
    // private ShipmentRepository shipmentRepository;
    // @Autowired
    // private MovingServiceRepository movingServiceRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @RequestParam(defaultValue = "30") int timeRange) {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(timeRange);

            // Get all bookings in date range
            List<Booking> bookings = bookingRepository.findAll().stream()
                    .filter(b -> {
                        Date createdAt = b.getCreatedAt();
                        if (createdAt == null) return false;
                        LocalDate bookingDate = createdAt.toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();
                        return !bookingDate.isBefore(startDate) && !bookingDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());

            // Calculate metrics
            Map<String, Object> metrics = calculateMetrics(bookings, startDate, endDate);

            // Calculate charts data
            Map<String, Object> charts = calculateCharts(bookings, startDate, endDate);

            // Calculate performance data
            Map<String, Object> performance = calculatePerformance(bookings);

            Map<String, Object> response = new HashMap<>();
            response.put("metrics", metrics);
            response.put("charts", charts);
            response.put("performance", performance);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to load analytics: " + e.getMessage()
            ));
        }
    }

    private Map<String, Object> calculateMetrics(List<Booking> bookings, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> metrics = new HashMap<>();

        // Current period stats
        long totalShipments = bookings.size();
        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> b.getTotalAmount() != null)
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Active customers (unique emails)
        long activeCustomers = bookings.stream()
                .map(Booking::getCustomerEmail)
                .distinct()
                .count();

        // Average rating (if available)
        double avgRating = 4.5; // Default, can be calculated from reviews if available

        // Previous period for growth calculation
        LocalDate prevStartDate = startDate.minusDays(endDate.toEpochDay() - startDate.toEpochDay());
        LocalDate prevEndDate = startDate;
        List<Booking> prevBookings = bookingRepository.findAll().stream()
                .filter(b -> {
                    Date createdAt = b.getCreatedAt();
                    if (createdAt == null) return false;
                    LocalDate bookingDate = createdAt.toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                    return !bookingDate.isBefore(prevStartDate) && bookingDate.isBefore(prevEndDate);
                })
                .collect(Collectors.toList());

        long prevShipments = prevBookings.size();
        BigDecimal prevRevenue = prevBookings.stream()
                .filter(b -> b.getTotalAmount() != null)
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long prevCustomers = prevBookings.stream()
                .map(Booking::getCustomerEmail)
                .distinct()
                .count();

        // Calculate growth percentages
        double shipmentsGrowth = prevShipments > 0 
                ? ((double)(totalShipments - prevShipments) / prevShipments) * 100 
                : 0;
        double revenueGrowth = prevRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalRevenue.subtract(prevRevenue).divide(prevRevenue, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0;
        double customersGrowth = prevCustomers > 0
                ? ((double)(activeCustomers - prevCustomers) / prevCustomers) * 100
                : 0;

        metrics.put("totalShipments", totalShipments);
        metrics.put("totalRevenue", totalRevenue.doubleValue());
        metrics.put("activeCustomers", activeCustomers);
        metrics.put("avgRating", avgRating);
        metrics.put("shipmentsGrowth", Math.round(shipmentsGrowth * 10.0) / 10.0);
        metrics.put("revenueGrowth", Math.round(revenueGrowth * 10.0) / 10.0);
        metrics.put("customersGrowth", Math.round(customersGrowth * 10.0) / 10.0);
        metrics.put("ratingGrowth", 0.0);

        return metrics;
    }

    private Map<String, Object> calculateCharts(List<Booking> bookings, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> charts = new HashMap<>();

        // Revenue chart data (daily)
        Map<String, BigDecimal> dailyRevenue = new LinkedHashMap<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            final LocalDate date = current;
            BigDecimal dayRevenue = bookings.stream()
                    .filter(b -> {
                        Date createdAt = b.getCreatedAt();
                        if (createdAt == null) return false;
                        LocalDate bookingDate = createdAt.toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();
                        return bookingDate.equals(date);
                    })
                    .filter(b -> b.getTotalAmount() != null)
                    .map(Booking::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dailyRevenue.put(date.format(DateTimeFormatter.ofPattern("MMM dd")), dayRevenue);
            current = current.plusDays(1);
        }

        Map<String, Object> revenueChart = new HashMap<>();
        revenueChart.put("labels", new ArrayList<>(dailyRevenue.keySet()));
        revenueChart.put("data", dailyRevenue.values().stream()
                .map(BigDecimal::doubleValue)
                .collect(Collectors.toList()));

        // Shipments chart data (by status)
        Map<String, Long> statusCounts = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getStatus().toString(),
                        Collectors.counting()
                ));

        Map<String, Object> shipmentsChart = new HashMap<>();
        shipmentsChart.put("data", Arrays.asList(
                statusCounts.getOrDefault("DELIVERED", 0L),
                statusCounts.getOrDefault("IN_TRANSIT", 0L) + statusCounts.getOrDefault("OUT_FOR_DELIVERY", 0L),
                statusCounts.getOrDefault("PENDING", 0L) + statusCounts.getOrDefault("PAYMENT_PENDING", 0L),
                statusCounts.getOrDefault("CANCELLED", 0L)
        ));

        // Geographic distribution (by delivery city)
        Map<String, Long> cityCounts = bookings.stream()
                .filter(b -> b.getDeliveryCity() != null)
                .collect(Collectors.groupingBy(
                        Booking::getDeliveryCity,
                        Collectors.counting()
                ));

        List<String> topCities = cityCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Map<String, Object> geographicChart = new HashMap<>();
        geographicChart.put("labels", topCities);
        geographicChart.put("data", topCities.stream()
                .map(city -> cityCounts.getOrDefault(city, 0L))
                .collect(Collectors.toList()));

        // Peak hours analysis
        Map<Integer, Long> hourCounts = bookings.stream()
                .filter(b -> b.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        b -> {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(b.getCreatedAt());
                            return cal.get(Calendar.HOUR_OF_DAY);
                        },
                        Collectors.counting()
                ));

        List<String> hourLabels = new ArrayList<>();
        List<Long> hourData = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            hourLabels.add(String.format("%02d:00", hour));
            hourData.add(hourCounts.getOrDefault(hour, 0L));
        }

        Map<String, Object> peakHoursChart = new HashMap<>();
        peakHoursChart.put("labels", hourLabels);
        peakHoursChart.put("data", hourData);

        charts.put("revenue", revenueChart);
        charts.put("shipments", shipmentsChart);
        charts.put("geographic", geographicChart);
        charts.put("peakHours", peakHoursChart);

        return charts;
    }

    private Map<String, Object> calculatePerformance(List<Booking> bookings) {
        Map<String, Object> performance = new HashMap<>();

        // Top drivers (simplified - would need driver assignment data)
        List<Map<String, Object>> topDrivers = new ArrayList<>();
        // This would need to query driver assignments and calculate stats
        // For now, return empty list
        performance.put("topDrivers", topDrivers);

        // Customer satisfaction (simplified)
        Map<String, Integer> satisfaction = new HashMap<>();
        satisfaction.put("fiveStars", 75);
        satisfaction.put("fourStars", 20);
        satisfaction.put("threeStars", 5);
        performance.put("satisfaction", satisfaction);

        // Service performance
        Map<String, Long> serviceCounts = bookings.stream()
                .filter(b -> b.getServiceType() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getServiceType().toString(),
                        Collectors.counting()
                ));

        List<Map<String, Object>> servicePerformance = serviceCounts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> service = new HashMap<>();
                    service.put("name", entry.getKey().replace("_", " "));
                    service.put("orders", entry.getValue());
                    
                    BigDecimal serviceRevenue = bookings.stream()
                            .filter(b -> b.getServiceType() != null && b.getServiceType().toString().equals(entry.getKey()))
                            .filter(b -> b.getTotalAmount() != null)
                            .map(Booking::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    service.put("revenue", serviceRevenue.doubleValue());
                    service.put("growth", 0); // Would need historical data
                    return service;
                })
                .sorted((a, b) -> Long.compare((Long)b.get("orders"), (Long)a.get("orders")))
                .limit(5)
                .collect(Collectors.toList());

        performance.put("service", servicePerformance);

        return performance;
    }
}

