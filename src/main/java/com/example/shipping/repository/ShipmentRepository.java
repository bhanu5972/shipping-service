package com.example.shipping.repository;

import com.example.shipping.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    
    List<Shipment> findByOrderId(UUID orderId);
    
    List<Shipment> findByStatus(String status);
    
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    
    List<Shipment> findByOrderIdAndStatus(UUID orderId, String status);
}