package com.example.RealEstate2.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 REQUIRED FIELD #1
    @Column(name = "offchain_hash")
    private String offchainHash;

    // 🔥 REQUIRED FIELD #2
    @Column(name = "blockchain_hash")
    private String blockchainHash;

    public String getOffchainHash() {
        return offchainHash;
    }

    public void setOffchainHash(String offchainHash) {
        this.offchainHash = offchainHash;
    }

    public String getBlockchainHash() {
        return blockchainHash;
    }

    public void setBlockchainHash(String blockchainHash) {
        this.blockchainHash = blockchainHash;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // 🔥 REQUIRED FIELD #3
    private LocalDateTime timestamp;
    // ID of the property that was transferred
    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    // UniqueId or user_id? (using unique id for safety)
    @Column(name = "buyer_id", nullable = false)
    private String buyerId;

    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    // Blockchain-style hash for transfer verification
    @Column(name = "tx_hash", nullable = false, unique = true)
    private String hash;

    // Date and time of transfer
    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime dateTime = LocalDateTime.now();

    // ------------------- Getters & Setters -------------------

    public Long getId() {
        return id;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
