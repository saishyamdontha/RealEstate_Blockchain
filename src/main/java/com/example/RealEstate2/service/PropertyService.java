package com.example.RealEstate2.service;

import com.example.RealEstate2.model.Property;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
public interface PropertyService {

    String getCheckProperty(String propertyId);

    ResponseEntity<?> getPropertyUser(Property property, String uniqueUserId, long userId);

    List<Property> getPropertiesByUser(long userId);

    ResponseEntity<?> deleteProperty(long userId, long propertyId);

    List<Property> getPropertiesForSale();

    // FINAL return type (no DTO)
    String transferProperty(Long propertyId, String sellerId, String buyerId);

    // Escrow flow (blockchainPropertyId = on-chain uint256 ID)
    void recordPendingBuyer(Long blockchainPropertyId, String buyerUniqueId);

    Map<String, Object> finalizeEscrowSale(Long blockchainPropertyId) throws Exception;

    void clearPendingBuyer(Long blockchainPropertyId);
}

