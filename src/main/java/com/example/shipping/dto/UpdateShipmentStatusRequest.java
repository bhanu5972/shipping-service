package com.example.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class UpdateShipmentStatusRequest {
    
    @NotNull(message = "Shipment ID is required")
    private UUID shipmentId;
    
    @NotBlank(message = "Status is required")
    private String status;  // SHIPPED, DELIVERED, CANCELLED
    
    private String trackingNumber;
    
    // Getters and Setters
    public UUID getShipmentId() { return shipmentId; }
    public void setShipmentId(UUID shipmentId) { this.shipmentId = shipmentId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
}