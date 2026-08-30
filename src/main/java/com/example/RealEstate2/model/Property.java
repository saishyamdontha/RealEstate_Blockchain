package com.example.RealEstate2.model;

import com.example.RealEstate2.model.status.PropertyStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_hash")
    private String propertyHash;

    public String getPropertyHash() {
        return propertyHash;
    }

    public void setPropertyHash(String propertyHash) {
        this.propertyHash = propertyHash;
    }

    // Your custom property ID (public ID)
    private String propertyId;

    // On-chain property ID from PropertyLedger.registerProperty() -- the
    // uint256 the contract actually indexes sales by. Distinct from
    // propertyId above (a separate app-level public ID) and from id (the
    // DB primary key). Null until the property has been registered on-chain.
    @Column(name = "blockchain_property_id")
    private Long blockchainPropertyId;

    private String titleDeedNumber;
    private String propertyType;
    private String address;
    private String city;

    private String latitude;
    private String longitude;

    // Marks property as for sale
    @Column(name = "for_sale", nullable = false)
    private boolean forSale = false;

    // Track approval or sale status
    @Enumerated(EnumType.STRING)
    @Column(name = "property_status")
    private PropertyStatus propertyStatus = PropertyStatus.PENDING;

    // Many properties belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    @JsonBackReference
    private User user;

    // Shortcut for user unique ID
    private String uniqueUserId;

    // ----------------------- Getters & Setters -----------------------

    public Long getId() {
        return id;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public Long getBlockchainPropertyId() {
        return blockchainPropertyId;
    }
    public void setBlockchainPropertyId(Long blockchainPropertyId) {
        this.blockchainPropertyId = blockchainPropertyId;
    }

    public String getTitleDeedNumber() {
        return titleDeedNumber;
    }

    public void setTitleDeedNumber(String titleDeedNumber) {
        this.titleDeedNumber = titleDeedNumber;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public boolean isForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public PropertyStatus getPropertyStatus() {
        return propertyStatus;
    }

    public void setPropertyStatus(PropertyStatus propertyStatus) {
        this.propertyStatus = propertyStatus;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUniqueUserId() {
        return uniqueUserId;
    }

    public void setUniqueUserId(String uniqueUserId) {
        this.uniqueUserId = uniqueUserId;
    }
}
