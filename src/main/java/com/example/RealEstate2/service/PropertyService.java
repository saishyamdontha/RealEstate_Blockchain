package com.example.RealEstate2.service;

import com.example.RealEstate2.model.Property;
import org.springframework.http.ResponseEntity;

import java.util.List;
public interface PropertyService {

    String getCheckProperty(String propertyId);

    ResponseEntity<?> getPropertyUser(Property property, String uniqueUserId, long userId);

    List<Property> getPropertiesByUser(long userId);

    ResponseEntity<?> deleteProperty(long userId, long propertyId);

    List<Property> getPropertiesForSale();

    // FINAL return type (no DTO)
    String transferProperty(Long propertyId, String sellerId, String buyerId);
}

