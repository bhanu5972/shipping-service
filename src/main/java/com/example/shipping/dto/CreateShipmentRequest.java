package com.example.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class CreateShipmentRequest {
    
    @NotNull(message = "Order ID is required")
    private UUID orderId;
    
    private String carrier;
    
    private String shippingAddress;
    
    private String itemsShipped;
    
    private LocalDateTime estimatedDelivery;
    
    // Getters and Setters
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public String getItemsShipped() { return itemsShipped; }
    public void setItemsShipped(String itemsShipped) { this.itemsShipped = itemsShipped; }
    
    public LocalDateTime getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
}