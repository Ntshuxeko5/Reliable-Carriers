package com.reliablecarriers.Reliable.Carriers.model;

public enum WebhookEvent {
    SHIPMENT_CREATED("shipment.created", "Triggered when a new shipment is created"),
    SHIPMENT_UPDATED("shipment.updated", "Triggered when shipment status changes"),
    SHIPMENT_DELIVERED("shipment.delivered", "Triggered when shipment is delivered"),
    SHIPMENT_PICKED_UP("shipment.picked_up", "Triggered when shipment is picked up"),
    PAYMENT_COMPLETED("payment.completed", "Triggered when payment is completed"),
    PAYMENT_FAILED("payment.failed", "Triggered when payment fails");
    
    private final String eventType;
    private final String description;
    
    WebhookEvent(String eventType, String description) {
        this.eventType = eventType;
        this.description = description;
    }
    
    public String getEventType() { return eventType; }
    public String getDescription() { return description; }
}





