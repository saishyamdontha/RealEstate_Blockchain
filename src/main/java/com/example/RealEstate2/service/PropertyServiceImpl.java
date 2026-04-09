package com.example.RealEstate2.service;

import com.example.RealEstate2.blockchain.PropertyLedger;
import com.example.RealEstate2.model.Property;
import com.example.RealEstate2.model.Transaction;
import com.example.RealEstate2.model.User;
import com.example.RealEstate2.model.status.PropertyStatus;
import com.example.RealEstate2.repository.PropertyRepository;
import com.example.RealEstate2.repository.TransactionRepository;
import com.example.RealEstate2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PropertyServiceImpl implements PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyLedgerService ledgerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;


    @Autowired private Web3j web3j;
    @Autowired private Credentials credentials;
    @Autowired private ContractGasProvider contractGasProvider;
    @Autowired private String contractAddress;

    // YOU MUST SET YOUR DEPLOYED SMART CONTRACT ADDRESS HERE
    private final String CONTRACT_ADDRESS = "0xYourDeployedContractAddress";

    // ---------------------------------------------------
    // CHECK PROPERTY EXISTS
    // ---------------------------------------------------
    @Override
    public String getCheckProperty(String propertyId) {
        boolean exists = propertyRepository.existsByPropertyId(propertyId);
        return exists ? "FOUND" : "NOT_FOUND";
    }

    // ---------------------------------------------------
    // ADD PROPERTY WITH USER
    // ---------------------------------------------------
    @Override
    public ResponseEntity<?> getPropertyUser(Property property, String uniqueUserId, long userId) {

        boolean exists =
                propertyRepository.existsByPropertyId(property.getPropertyId()) ||
                        propertyRepository.existsByAddress(property.getAddress()) ||
                        propertyRepository.existsByLatitudeAndLongitude(property.getLatitude(), property.getLongitude()) ||
                        propertyRepository.existsByTitleDeedNumber(property.getTitleDeedNumber());

        if (exists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Property already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        property.setUser(user);
        property.setUniqueUserId(uniqueUserId);
        property.setForSale(false);
        property.setPropertyStatus(PropertyStatus.PENDING);

        propertyRepository.save(property);

        return ResponseEntity.ok("Property added successfully");
    }

    // ---------------------------------------------------
    // GET PROPERTIES BY USER
    // ---------------------------------------------------
    @Override
    public List<Property> getPropertiesByUser(long userId) {
        return propertyRepository.findByUserId(userId);
    }

    // ---------------------------------------------------
    // DELETE PROPERTY
    // ---------------------------------------------------
    @Override
    public ResponseEntity<?> deleteProperty(long userId, long propertyId) {

        Property property = propertyRepository.findById(propertyId).orElse(null);

        if (property == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Property not found");
        }

        if (property.getUser() == null || property.getUser().getId() != userId) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Property does not belong to this user");
        }

        propertyRepository.delete(property);

        return ResponseEntity.ok("Property deleted successfully");
    }

    // ---------------------------------------------------
    // GET PROPERTIES FOR SALE
    // ---------------------------------------------------
    @Override
    public List<Property> getPropertiesForSale() {
        return propertyRepository.findByForSaleTrue();
    }


    // ---------------------------------------------------
    // TRANSFER PROPERTY (BLOCKCHAIN + DB)
    // ---------------------------------------------------
    @Override
    public String transferProperty(Long propertyId, String sellerId, String buyerId) {

        // 1. Fetch property
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        // 2. Validate seller owns property
        if (!property.getUniqueUserId().equals(sellerId)) {
            throw new RuntimeException("Seller does not own this property");
        }

        // 3. Fetch buyer
        User buyer = userRepository.findByUniqueId(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        // 4. Generate LOCAL offchain hash
        String offchainHash = "OFFCHAIN-" + propertyId + "-" + System.nanoTime();

        // 5. Blockchain call
        String blockchainTxHash = "";
        try {
            PropertyLedger ledger = PropertyLedger.load(
                    contractAddress, web3j, credentials, contractGasProvider
            );

            var receipt = ledger.recordTransfer(
                    BigInteger.valueOf(propertyId),
                    sellerId,
                    buyerId,
                    offchainHash
            ).send();

            blockchainTxHash = receipt.getTransactionHash();

        } catch (Exception e) {
            throw new RuntimeException("Blockchain transaction failed: " + e.getMessage());
        }

        // 6. Update property in DB
        property.setUser(buyer);
        property.setUniqueUserId(buyerId);
        property.setForSale(false);
        property.setPropertyStatus(PropertyStatus.TRANSFERRED);
        propertyRepository.save(property);

        // 7. Save offchain transaction record
        Transaction tx = new Transaction();
        tx.setPropertyId(propertyId);
        tx.setSellerId(sellerId);
        tx.setBuyerId(buyerId);
        tx.setHash(offchainHash);
        tx.setOffchainHash(offchainHash);
        tx.setBlockchainHash(blockchainTxHash);
        tx.setTimestamp(LocalDateTime.now());
        transactionRepository.save(tx);

        return "Property transferred successfully! Blockchain TX: " + blockchainTxHash;
    }
}
