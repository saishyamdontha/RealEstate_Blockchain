package com.example.RealEstate2.controller;

import com.example.RealEstate2.model.Property;
import com.example.RealEstate2.repository.PropertyRepository;
import com.example.RealEstate2.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private PropertyService propertyService;
    @Autowired
    private PropertyRepository propertyRepository;

    // ADD PROPERTY
    @PostMapping("/add/property/{userId}/{uniqueId}")
    public ResponseEntity<?> addProperty(
            @RequestBody Property property,
            @PathVariable long userId,
            @PathVariable String uniqueId) {

        String propertyId = property.getPropertyId();
        if (propertyId == null || propertyId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Please enter property id");
        }

        String result = propertyService.getCheckProperty(propertyId);
        if ("FOUND".equals(result)) {
            return ResponseEntity.status(409)
                    .body("Property already exists.\nEnter a different property ID.");
        }

        return propertyService.getPropertyUser(property, uniqueId, userId);
    }

    // GET ALL PROPERTIES OF A USER
    @GetMapping("/{userId}/properties")
    public ResponseEntity<List<Property>> getUserProperties(@PathVariable long userId) {
        List<Property> properties = propertyService.getPropertiesByUser(userId);
        return ResponseEntity.ok(properties);
    }

    // DELETE PROPERTY OF A USER
    @DeleteMapping("/{userId}/delete/property/{propertyId}")
    public ResponseEntity<?> deleteUserProperty(
            @PathVariable long userId,
            @PathVariable long propertyId) {
        return propertyService.deleteProperty(userId, propertyId);
    }
    @PutMapping("/property/sell/{propertyId}/{userUniqueId}")
    public ResponseEntity<?> editSellProperty(@PathVariable Long propertyId,
                                              @PathVariable String userUniqueId) {

        // 1. Find property
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found with ID: " + propertyId));

        // 2. Validate that the given user is the owner
        if (!property.getUser().getUniqueId().equals(userUniqueId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("This user is not the owner of this property!");
        }

        // 3. Mark as FOR SALE
        property.setForSale(true);
//        property.setStatus("FOR_SALE");

        // 4. Save
        propertyRepository.save(property);

        return ResponseEntity.ok("Property marked as FOR SALE by owner.");
    }
    @GetMapping("/properties/for-sale")
    public ResponseEntity<?> getPropertiesForSale() {
        List<Property> properties = propertyService.getPropertiesForSale();
        return ResponseEntity.ok(properties);
    }
    //---------Transfer of property here---------
    @PutMapping("/property/transfer/{propertyId}/{sellerId}/{buyerId}")
    public ResponseEntity<?> transferProperty(
            @PathVariable Long propertyId,
            @PathVariable String sellerId,
            @PathVariable String buyerId) {

        String message = propertyService.transferProperty(propertyId, sellerId, buyerId);
        return ResponseEntity.ok(message);
    }

}