package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.service.AnalyticsService;
import com.reliablecarriers.Reliable.Carriers.service.CustomerTierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private ShipmentRepository shipmentRepository;
    
    @Autowired
    private CustomerTierService customerTierService;

    @Override
    public AnalyticsData generateCustomerAnalytics(User customer) {
        if (!hasAnalyticsAccess(customer)) {
            throw new SecurityException("Customer does not have analytics access");
        }
        
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        return generateCustomerAnalytics(customer, sixMonthsAgo, LocalDateTime.now());
    }

    @Override
    public AnalyticsData generateCustomerAnalytics(User customer, LocalDateTime startDate, LocalDateTime endDate) {
        if (!hasAnalyticsAccess(customer)) {
            throw new SecurityException("Customer does not have analytics access");
        }
        
        AnalyticsData analytics = new AnalyticsData();
        analytics.setCustomerId(customer.getId().toString());
        analytics.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
        analytics.setCustomerTier(customer.getCustomerTier());
        analytics.setGeneratedAt(LocalDateTime.now());
        
        // Get shipments for the customer in the date range
        List<Shipment> shipments = shipmentRepository.findBySenderEmailAndCreatedAtBetween(
            customer.getEmail(), startDate, endDate);
        
        // Calculate basic statistics
        calculateBasicStatistics(analytics, shipments);
        
        // Calculate performance metrics
        calculatePerformanceMetrics(analytics, shipments);
        
        // Generate trends and patterns
        generateTrendsAndPatterns(analytics, shipments);
        
        // Generate recommendations
        generateRecommendations(analytics, shipments);
        
        return analytics;
    }

    @Override
    public DeliveryPerformanceMetrics getDeliveryPerformance(User customer) {
        if (!hasAnalyticsAccess(customer)) {
            throw new SecurityException("Customer does not have analytics access");
        }
        
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Shipment> shipments = shipmentRepository.findBySenderEmailAndCreatedAtBetween(
            customer.getEmail(), sixMonthsAgo, LocalDateTime.now());
        
        DeliveryPerformanceMetrics metrics = new DeliveryPerformanceMetrics();
        
        // Calculate on-time delivery rate
        long onTimeDeliveries = shipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .filter(this::isOnTimeDelivery)
            .count();
        
        long totalDeliveries = shipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .count();
        
        if (totalDeliveries > 0) {
            metrics.setOnTimeDeliveryRate((double) onTimeDeliveries / totalDeliveries * 100);
        }
        
        // Calculate average delivery time
        double avgDeliveryTime = shipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .mapToDouble(this::calculateDeliveryTime)
            .average()
            .orElse(0.0);
        
        metrics.setAverageDeliveryTime(avgDeliveryTime);
        
        // Calculate delays
        long delays = shipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .filter(s -> !isOnTimeDelivery(s))
            .count();
        
        metrics.setTotalDelays((int) delays);
        metrics.setDelayRate(totalDeliveries > 0 ? (double) delays / totalDeliveries * 100 : 0.0);
        
        // Find best and worst performing routes
        Map<String, Double> routePerformance = calculateRoutePerformance(shipments);
        if (!routePerformance.isEmpty()) {
            String bestRoute = routePerformance.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
            String worstRoute = routePerformance.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
            
            metrics.setBestPerformingRoute(bestRoute);
            metrics.setWorstPerformingRoute(worstRoute);
        }
        
        return metrics;
    }

    @Override
    public CostAnalysis getCostAnalysis(User customer) {
        if (!hasAnalyticsAccess(customer)) {
            throw new SecurityException("Customer does not have analytics access");
        }
        
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Shipment> shipments = shipmentRepository.findBySenderEmailAndCreatedAtBetween(
            customer.getEmail(), sixMonthsAgo, LocalDateTime.now());
        
        CostAnalysis analysis = new CostAnalysis();
        
        // Calculate total spent
        double totalSpent = shipments.stream()
            .mapToDouble(s -> s.getShippingCost().doubleValue())
            .sum();
        analysis.setTotalSpent(totalSpent);
        
        // Calculate average cost per shipment
        double avgCost = shipments.isEmpty() ? 0.0 : totalSpent / shipments.size();
        analysis.setAverageCostPerShipment(avgCost);
        
        // Calculate potential savings (simplified logic)
        double potentialSavings = calculatePotentialSavings(shipments);
        analysis.setPotentialSavings(potentialSavings);
        
        // Find most expensive and cost-effective services
        Map<String, Double> serviceCosts = shipments.stream()
            .collect(Collectors.groupingBy(
                s -> s.getServiceType().toString(),
                Collectors.averagingDouble(s -> s.getShippingCost().doubleValue())
            ));
        
        if (!serviceCosts.isEmpty()) {
            String mostExpensive = serviceCosts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
            String mostCostEffective = serviceCosts.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
            
            analysis.setMostExpensiveService(mostExpensive);
            analysis.setMostCostEffectiveService(mostCostEffective);
        }
        
        // Generate cost breakdown
        List<CostBreakdown> costBreakdown = generateCostBreakdown(shipments);
        analysis.setCostBreakdown(costBreakdown);
        
        return analysis;
    }

    @Override
    public ShippingTrends getShippingTrends(User customer) {
        if (!hasAnalyticsAccess(customer)) {
            throw new SecurityException("Customer does not have analytics access");
        }
        
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Shipment> shipments = shipmentRepository.findBySenderEmailAndCreatedAtBetween(
            customer.getEmail(), sixMonthsAgo, LocalDateTime.now());
        
        ShippingTrends trends = new ShippingTrends();
        
        // Generate monthly trends
        List<MonthlyTrend> monthlyTrends = generateMonthlyTrends(shipments);
        trends.setMonthlyTrends(monthlyTrends);
        
        // Find peak shipping day and time
        Map<String, Long> dayCounts = shipments.stream()
            .collect(Collectors.groupingBy(
                s -> s.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().getDayOfWeek().name(),
                Collectors.counting()
            ));
        
        String peakDay = dayCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        trends.setPeakShippingDay(peakDay);
        
        // Find peak shipping time (simplified)
        trends.setPeakShippingTime("Morning (9-12 AM)");
        
        // Generate popular routes
        List<PopularRoute> popularRoutes = generatePopularRoutes(shipments);
        trends.setPopularRoutes(popularRoutes);
        
        // Generate service usage
        List<ServiceUsage> serviceUsage = generateServiceUsage(shipments);
        trends.setServiceUsage(serviceUsage);
        
        return trends;
    }

    @Override
    public List<String> getOptimizationRecommendations(User customer) {
        if (!hasAnalyticsAccess(customer)) {
            throw new SecurityException("Customer does not have analytics access");
        }
        
        List<String> recommendations = new ArrayList<>();
        
        // Get analytics data
        AnalyticsData analytics = generateCustomerAnalytics(customer);
        
        // Generate recommendations based on data
        if (analytics.getOnTimeDeliveryRate() < 90) {
            recommendations.add("Consider using express delivery for time-sensitive packages");
        }
        
        if (analytics.getAverageShippingCost() > 500) {
            recommendations.add("Bulk shipping could reduce your average cost per package");
        }
        
        if (analytics.getTotalShipments() > 50) {
            recommendations.add("You qualify for business discounts - consider upgrading your plan");
        }
        
        if (analytics.getDelayRate() > 10) {
            recommendations.add("Review your packaging and address accuracy to reduce delays");
        }
        
        // Add tier-specific recommendations
        if (customer.getCustomerTier() == CustomerTier.INDIVIDUAL) {
            recommendations.add("Upgrade to Business plan for bulk shipping discounts");
        }
        
        return recommendations;
    }

    @Override
    public boolean hasAnalyticsAccess(User customer) {
        return customerTierService.hasAnalyticsAccess(customer);
    }

    @Override
    public byte[] generateAnalyticsReport(User customer, String format) {
        if (!hasAnalyticsAccess(customer)) {
            throw new SecurityException("Customer does not have analytics access");
        }
        
        // This would generate a PDF or Excel report
        // For now, return a simple text report
        AnalyticsData analytics = generateCustomerAnalytics(customer);
        String report = generateTextReport(analytics);
        return report.getBytes();
    }

    // Helper methods
    private void calculateBasicStatistics(AnalyticsData analytics, List<Shipment> shipments) {
        analytics.setTotalShipments(shipments.size());
        
        long delivered = shipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .count();
        analytics.setDeliveredShipments((int) delivered);
        
        long pending = shipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.PENDING)
            .count();
        analytics.setPendingShipments((int) pending);
        
        long inTransit = shipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT)
            .count();
        analytics.setInTransitShipments((int) inTransit);
        
        // Calculate on-time delivery rate
        long onTime = shipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .filter(this::isOnTimeDelivery)
            .count();
        
        if (delivered > 0) {
            analytics.setOnTimeDeliveryRate((double) onTime / delivered * 100);
        }
        
        // Calculate average delivery time
        double avgTime = shipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .mapToDouble(this::calculateDeliveryTime)
            .average()
            .orElse(0.0);
        analytics.setAverageDeliveryTime(avgTime);
        
        // Calculate costs
        double totalCost = shipments.stream()
            .mapToDouble(s -> s.getShippingCost().doubleValue())
            .sum();
        analytics.setTotalShippingCost(totalCost);
        analytics.setAverageShippingCost(shipments.isEmpty() ? 0.0 : totalCost / shipments.size());
    }

    private void calculatePerformanceMetrics(AnalyticsData analytics, List<Shipment> shipments) {
        // Calculate satisfaction score (simplified)
        analytics.setCustomerSatisfactionScore(4.2);
        
        // Calculate delays
        long delays = shipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .filter(s -> !isOnTimeDelivery(s))
            .count();
        analytics.setTotalDelays((int) delays);
        
        long delivered = shipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .count();
        analytics.setDelayRate(delivered > 0 ? (double) delays / delivered * 100 : 0.0);
        
        // Find most used service
        Map<String, Long> serviceCounts = shipments.stream()
            .collect(Collectors.groupingBy(
                s -> s.getServiceType().toString(),
                Collectors.counting()
            ));
        
        String mostUsed = serviceCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Standard");
        analytics.setMostUsedService(mostUsed);
        
        // Find peak delivery day and time
        analytics.setPeakDeliveryDay("Tuesday");
        analytics.setPeakDeliveryTime("10:00 AM - 2:00 PM");
    }

    private void generateTrendsAndPatterns(AnalyticsData analytics, List<Shipment> shipments) {
        // Generate monthly trends
        List<AnalyticsData.MonthlyData> monthlyData = generateMonthlyTrends(shipments).stream()
            .map(trend -> {
                AnalyticsData.MonthlyData data = new AnalyticsData.MonthlyData();
                data.setMonth(trend.getMonth());
                data.setShipments(trend.getShipments());
                data.setTotalCost(trend.getTotalCost());
                return data;
            })
            .collect(Collectors.toList());
        analytics.setMonthlyTrends(monthlyData);
        
        // Generate service type breakdown
        List<AnalyticsData.ServiceTypeData> serviceBreakdown = generateServiceUsage(shipments).stream()
            .map(usage -> {
                AnalyticsData.ServiceTypeData data = new AnalyticsData.ServiceTypeData();
                data.setServiceType(usage.getServiceType());
                data.setCount(usage.getCount());
                data.setPercentage(usage.getPercentage());
                return data;
            })
            .collect(Collectors.toList());
        analytics.setServiceTypeBreakdown(serviceBreakdown);
        
        // Generate popular routes
        List<AnalyticsData.RouteData> routeData = generatePopularRoutes(shipments).stream()
            .map(route -> {
                AnalyticsData.RouteData data = new AnalyticsData.RouteData();
                data.setFromCity(route.getFromCity());
                data.setToCity(route.getToCity());
                data.setFrequency(route.getFrequency());
                data.setAverageCost(route.getAverageCost());
                return data;
            })
            .collect(Collectors.toList());
        analytics.setPopularRoutes(routeData);
    }

    private void generateRecommendations(AnalyticsData analytics, List<Shipment> shipments) {
        List<String> recommendations = new ArrayList<>();
        List<String> optimizations = new ArrayList<>();
        
        // Generate recommendations based on analytics
        if (analytics.getOnTimeDeliveryRate() < 85) {
            recommendations.add("Consider upgrading to express delivery for better on-time performance");
        }
        
        if (analytics.getAverageShippingCost() > 300) {
            recommendations.add("Bulk shipping could reduce your average cost by 15-20%");
        }
        
        if (analytics.getTotalShipments() > 20) {
            recommendations.add("You may benefit from a business account with volume discounts");
        }
        
        // Generate optimization suggestions
        optimizations.add("Consolidate shipments to reduce costs");
        optimizations.add("Use standard delivery for non-urgent packages");
        optimizations.add("Consider scheduled pickups for better planning");
        
        analytics.setRecommendations(recommendations);
        analytics.setOptimizationSuggestions(optimizations);
    }

    private boolean isOnTimeDelivery(Shipment shipment) {
        // Simplified logic - in reality, this would compare with estimated delivery time
        return true; // Placeholder
    }

    private double calculateDeliveryTime(Shipment shipment) {
        // Simplified calculation - in reality, this would calculate actual delivery time
        return 24.0; // Placeholder - 24 hours
    }

    private Map<String, Double> calculateRoutePerformance(List<Shipment> shipments) {
        // Simplified route performance calculation
        return new HashMap<>();
    }

    private double calculatePotentialSavings(List<Shipment> shipments) {
        // Simplified savings calculation
        return shipments.size() * 50.0; // R50 per shipment potential savings
    }

    private List<CostBreakdown> generateCostBreakdown(List<Shipment> shipments) {
        List<CostBreakdown> breakdown = new ArrayList<>();
        
        CostBreakdown shipping = new CostBreakdown();
        shipping.setCategory("Shipping Costs");
        shipping.setAmount(shipments.stream().mapToDouble(s -> s.getShippingCost().doubleValue()).sum());
        shipping.setPercentage(100.0);
        breakdown.add(shipping);
        
        return breakdown;
    }

    private List<MonthlyTrend> generateMonthlyTrends(List<Shipment> shipments) {
        Map<String, List<Shipment>> monthlyShipments = shipments.stream()
            .collect(Collectors.groupingBy(
                s -> s.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            ));
        
        return monthlyShipments.entrySet().stream()
            .map(entry -> {
                MonthlyTrend trend = new MonthlyTrend();
                trend.setMonth(entry.getKey());
                trend.setShipments(entry.getValue().size());
                trend.setTotalCost(entry.getValue().stream()
                    .mapToDouble(s -> s.getShippingCost().doubleValue())
                    .sum());
                return trend;
            })
            .sorted(Comparator.comparing(MonthlyTrend::getMonth))
            .collect(Collectors.toList());
    }

    private List<PopularRoute> generatePopularRoutes(List<Shipment> shipments) {
        Map<String, List<Shipment>> routeGroups = shipments.stream()
            .collect(Collectors.groupingBy(
                s -> s.getPickupCity() + " -> " + s.getDeliveryCity()
            ));
        
        return routeGroups.entrySet().stream()
            .map(entry -> {
                PopularRoute route = new PopularRoute();
                String[] cities = entry.getKey().split(" -> ");
                route.setFromCity(cities[0]);
                route.setToCity(cities[1]);
                route.setFrequency(entry.getValue().size());
                route.setAverageCost(entry.getValue().stream()
                    .mapToDouble(s -> s.getShippingCost().doubleValue())
                    .average()
                    .orElse(0.0));
                return route;
            })
            .sorted(Comparator.comparing(PopularRoute::getFrequency).reversed())
            .limit(5)
            .collect(Collectors.toList());
    }

    private List<ServiceUsage> generateServiceUsage(List<Shipment> shipments) {
        Map<String, Long> serviceCounts = shipments.stream()
            .collect(Collectors.groupingBy(
                s -> s.getServiceType().toString(),
                Collectors.counting()
            ));
        
        long total = shipments.size();
        
        return serviceCounts.entrySet().stream()
            .map(entry -> {
                ServiceUsage usage = new ServiceUsage();
                usage.setServiceType(entry.getKey());
                usage.setCount(entry.getValue().intValue());
                usage.setPercentage(total > 0 ? (double) entry.getValue() / total * 100 : 0.0);
                return usage;
            })
            .sorted(Comparator.comparing(ServiceUsage::getCount).reversed())
            .collect(Collectors.toList());
    }

    private String generateTextReport(AnalyticsData analytics) {
        StringBuilder report = new StringBuilder();
        report.append("ANALYTICS REPORT\n");
        report.append("================\n\n");
        report.append("Customer: ").append(analytics.getCustomerName()).append("\n");
        report.append("Tier: ").append(analytics.getCustomerTier().getDisplayName()).append("\n");
        report.append("Generated: ").append(analytics.getGeneratedAt()).append("\n\n");
        
        report.append("SHIPMENT STATISTICS\n");
        report.append("Total Shipments: ").append(analytics.getTotalShipments()).append("\n");
        report.append("Delivered: ").append(analytics.getDeliveredShipments()).append("\n");
        report.append("Pending: ").append(analytics.getPendingShipments()).append("\n");
        report.append("In Transit: ").append(analytics.getInTransitShipments()).append("\n\n");
        
        report.append("PERFORMANCE METRICS\n");
        report.append("On-Time Delivery Rate: ").append(String.format("%.1f%%", analytics.getOnTimeDeliveryRate())).append("\n");
        report.append("Average Delivery Time: ").append(String.format("%.1f hours", analytics.getAverageDeliveryTime())).append("\n");
        report.append("Total Delays: ").append(analytics.getTotalDelays()).append("\n");
        report.append("Delay Rate: ").append(String.format("%.1f%%", analytics.getDelayRate())).append("\n\n");
        
        report.append("COST ANALYSIS\n");
        report.append("Total Shipping Cost: R").append(String.format("%.2f", analytics.getTotalShippingCost())).append("\n");
        report.append("Average Cost per Shipment: R").append(String.format("%.2f", analytics.getAverageShippingCost())).append("\n\n");
        
        return report.toString();
    }
}