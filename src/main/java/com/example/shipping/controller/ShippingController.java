package com.example.shipping.controller;

import com.example.shipping.dto.CreateShipmentRequest;
import com.example.shipping.dto.ShipmentResponse;
import com.example.shipping.dto.UpdateShipmentStatusRequest;
import com.example.shipping.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/shipments")
@Tag(name = "Shipment Management", description = "Endpoints for managing shipments")
public class ShippingController {
    
    private static final Logger log = LoggerFactory.getLogger(ShippingController.class);
    
    private final ShippingService shippingService;
    
    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }
    
    @PostMapping
    @Operation(summary = "Create a shipment", description = "Creates a new shipment for an order")
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        log.info("POST /v1/shipments - orderId: {}", request.getOrderId());
        ShipmentResponse response = shippingService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/status")
    @Operation(summary = "Update shipment status", description = "Updates the status of a shipment")
    public ResponseEntity<ShipmentResponse> updateShipmentStatus(@Valid @RequestBody UpdateShipmentStatusRequest request) {
        log.info("PUT /v1/shipments/status - shipmentId: {}, status: {}", request.getShipmentId(), request.getStatus());
        ShipmentResponse response = shippingService.updateShipmentStatus(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all shipments", description = "Returns all shipments")
    public ResponseEntity<List<ShipmentResponse>> getAllShipments() {
        log.info("GET /v1/shipments");
        return ResponseEntity.ok(shippingService.getAllShipments());
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get shipments by order ID", description = "Returns all shipments for an order")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsByOrderId(@PathVariable UUID orderId) {
        log.info("GET /v1/shipments/order/{}", orderId);
        return ResponseEntity.ok(shippingService.getShipmentsByOrderId(orderId));
    }
    
    @GetMapping("/{shipmentId}")
    @Operation(summary = "Get shipment by ID", description = "Returns a single shipment")
    public ResponseEntity<ShipmentResponse> getShipmentById(@PathVariable UUID shipmentId) {
        log.info("GET /v1/shipments/{}", shipmentId);
        return ResponseEntity.ok(shippingService.getShipmentById(shipmentId));
    }
    
    @GetMapping("/tracking/{trackingNumber}")
    @Operation(summary = "Get shipment by tracking number", description = "Returns a shipment by tracking number")
    public ResponseEntity<ShipmentResponse> getShipmentByTrackingNumber(@PathVariable String trackingNumber) {
        log.info("GET /v1/shipments/tracking/{}", trackingNumber);
        return ResponseEntity.ok(shippingService.getShipmentByTrackingNumber(trackingNumber));
    }
    
    @PostMapping("/{shipmentId}/cancel")
    @Operation(summary = "Cancel a shipment", description = "Cancels a pending shipment")
    public ResponseEntity<Void> cancelShipment(@PathVariable UUID shipmentId) {
        log.info("POST /v1/shipments/{}/cancel", shipmentId);
        shippingService.cancelShipment(shipmentId);
        return ResponseEntity.noContent().build();
    }
}