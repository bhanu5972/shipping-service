package com.example.shipping.service;

import com.example.shipping.dto.CreateShipmentRequest;
import com.example.shipping.dto.ShipmentResponse;
import com.example.shipping.dto.UpdateShipmentStatusRequest;
import com.example.shipping.entity.Shipment;
import com.example.shipping.repository.ShipmentRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShippingService {
    
    private static final Logger log = LoggerFactory.getLogger(ShippingService.class);
    
    private final ShipmentRepository shipmentRepository;
    
    // Metrics
    private final Counter shipmentsCreatedCounter;
    private final Counter shipmentsDeliveredCounter;
    
    public ShippingService(ShipmentRepository shipmentRepository, MeterRegistry meterRegistry) {
        this.shipmentRepository = shipmentRepository;
        
        this.shipmentsCreatedCounter = Counter.builder("shipments_created_total")
                .description("Total number of shipments created")
                .register(meterRegistry);
        
        this.shipmentsDeliveredCounter = Counter.builder("shipments_delivered_total")
                .description("Total number of shipments delivered")
                .register(meterRegistry);
    }
    
    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        log.info("Creating shipment for order: {}", request.getOrderId());
        
        // Check if shipment already exists for this order
        List<Shipment> existingShipments = shipmentRepository.findByOrderId(request.getOrderId());
        if (!existingShipments.isEmpty()) {
            log.info("Shipment already exists for order: {}", request.getOrderId());
            return mapToResponse(existingShipments.get(0));
        }
        
        // Generate tracking number
        String trackingNumber = generateTrackingNumber(request.getCarrier());
        
        Shipment shipment = new Shipment();
        shipment.setOrderId(request.getOrderId());
        shipment.setCarrier(request.getCarrier() != null ? request.getCarrier() : "FedEx");
        shipment.setStatus("PENDING");
        shipment.setTrackingNumber(trackingNumber);
        shipment.setShippingAddress(request.getShippingAddress());
        shipment.setItemsShipped(request.getItemsShipped());
        shipment.setEstimatedDelivery(request.getEstimatedDelivery());
        
        shipment = shipmentRepository.save(shipment);
        
        shipmentsCreatedCounter.increment();
        
        log.info("Shipment created with ID: {}, tracking: {}", shipment.getShipmentId(), trackingNumber);
        
        return mapToResponse(shipment);
    }
    
    @Transactional
    public ShipmentResponse updateShipmentStatus(UpdateShipmentStatusRequest request) {
        log.info("Updating shipment status: {} to {}", request.getShipmentId(), request.getStatus());
        
        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + request.getShipmentId()));
        
        shipment.setStatus(request.getStatus());
        
        if (request.getTrackingNumber() != null) {
            shipment.setTrackingNumber(request.getTrackingNumber());
        }
        
        if ("SHIPPED".equals(request.getStatus()) && shipment.getShippedAt() == null) {
            shipment.setShippedAt(LocalDateTime.now());
            log.info("Shipment marked as SHIPPED: {}", request.getShipmentId());
        }
        
        if ("DELIVERED".equals(request.getStatus()) && shipment.getDeliveredAt() == null) {
            shipment.setDeliveredAt(LocalDateTime.now());
            shipmentsDeliveredCounter.increment();
            log.info("Shipment marked as DELIVERED: {}", request.getShipmentId());
        }
        
        if ("CANCELLED".equals(request.getStatus())) {
            log.info("Shipment cancelled: {}", request.getShipmentId());
        }
        
        shipment = shipmentRepository.save(shipment);
        
        return mapToResponse(shipment);
    }
    
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getShipmentsByOrderId(UUID orderId) {
        log.debug("Fetching shipments for order: {}", orderId);
        return shipmentRepository.findByOrderId(orderId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(UUID shipmentId) {
        log.debug("Fetching shipment by ID: {}", shipmentId);
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
        return mapToResponse(shipment);
    }
    
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByTrackingNumber(String trackingNumber) {
        log.debug("Fetching shipment by tracking: {}", trackingNumber);
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipment not found with tracking: " + trackingNumber));
        return mapToResponse(shipment);
    }
    
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getAllShipments() {
        log.debug("Fetching all shipments");
        return shipmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void cancelShipment(UUID shipmentId) {
        log.info("Cancelling shipment: {}", shipmentId);
        
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
        
        if ("SHIPPED".equals(shipment.getStatus()) || "DELIVERED".equals(shipment.getStatus())) {
            throw new RuntimeException("Cannot cancel shipment that is already " + shipment.getStatus());
        }
        
        shipment.setStatus("CANCELLED");
        shipmentRepository.save(shipment);
        
        log.info("Shipment cancelled: {}", shipmentId);
    }
    
    private String generateTrackingNumber(String carrier) {
        String prefix = "TRK";
        if ("FedEx".equalsIgnoreCase(carrier)) prefix = "FDX";
        else if ("UPS".equalsIgnoreCase(carrier)) prefix = "UPS";
        else if ("DHL".equalsIgnoreCase(carrier)) prefix = "DHL";
        
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private ShipmentResponse mapToResponse(Shipment shipment) {
        ShipmentResponse response = new ShipmentResponse();
        response.setShipmentId(shipment.getShipmentId());
        response.setOrderId(shipment.getOrderId());
        response.setCarrier(shipment.getCarrier());
        response.setStatus(shipment.getStatus());
        response.setTrackingNumber(shipment.getTrackingNumber());
        response.setShippedAt(shipment.getShippedAt());
        response.setDeliveredAt(shipment.getDeliveredAt());
        response.setEstimatedDelivery(shipment.getEstimatedDelivery());
        response.setCreatedAt(shipment.getCreatedAt());
        return response;
    }
}