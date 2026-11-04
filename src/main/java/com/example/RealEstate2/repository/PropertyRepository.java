package com.example.RealEstate2.repository;

import com.example.RealEstate2.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property,Long> {

    boolean existsByPropertyId(String propertyId);

    boolean existsByAddress(String address);

    boolean existsByLatitudeAndLongitude(String latitude, String longitude);

    boolean existsByTitleDeedNumber(String titleDeedNumber);

    List<Property> findByUserId(Long userId);
    List<Property> findByForSaleTrue();

    Optional<Property> findByBlockchainPropertyId(Long blockchainPropertyId);
}
