package com.example.RealEstate2.service;

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

        // Register on-chain before saving to DB: a Property row must never
        // exist without a corresponding blockchainPropertyId, since
        // transferProperty() (and any future escrow action) requires it.
        // If this call fails, the whole creation fails -- no DB row is
        // created for a property that doesn't exist on-chain.
        java.math.BigInteger onChainId;
        try {
            onChainId = ledgerService.registerProperty();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to register property on-chain: " + e.getMessage());
        }

        property.setUser(user);
        property.setUniqueUserId(uniqueUserId);
        property.setForSale(false);
        property.setPropertyStatus(PropertyStatus.PENDING);
        property.setBlockchainPropertyId(onChainId.longValueExact());

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

        // Every property now gets a blockchainPropertyId at creation time
        // (see getPropertyUser), so on-chain state always exists by the
        // time deletion is possible. Deleting the DB row does NOT touch
        // the chain -- the on-chain record would become orphaned (still
        // live, but unreachable from this app). Block deletion once a
        // property is actively listed for sale (forSale=true), since that
        // means an escrow flow may already be in progress on-chain and
        // deleting the DB row here would desync app state from chain
        // state mid-sale. Properties not currently listed can still be
        // deleted -- their on-chain registration remains as an inert,
        // orphaned record, which is a known limitation (see README).
        if (property.isForSale()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot delete a property that is currently listed for sale "
                            + "(on-chain escrow may be in progress). Cancel the sale first.");
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
        // NOTE: this now assumes listProperty -> depositEarnest ->
        // updateInspectionStatus -> fundAsLender have already completed
        // on-chain for the property's blockchainPropertyId via the escrow
        // endpoints. This method only triggers the final on-chain ownership
        // transfer + payout -- it is no longer a single atomic "record a
        // transfer" call like the old contract's recordTransfer was.
        //
        // Uses property.getBlockchainPropertyId() (the contract's uint256
        // propertyId), NOT the DB primary key passed into this method --
        // those are different ID spaces. blockchainPropertyId is null until
        // a property has actually been registered on-chain via
        // PropertyLedgerService.registerProperty(); there is currently no
        // endpoint that does this as part of property creation, so this
        // will fail until that's wired up.
        if (property.getBlockchainPropertyId() == null) {
            throw new IllegalStateException(
                    "Property " + propertyId + " has not been registered on-chain "
                            + "(blockchainPropertyId is null) -- cannot finalize a sale for it."
            );
        }
        String blockchainTxHash;
        try {
            blockchainTxHash = ledgerService.finalizeSale(property.getBlockchainPropertyId());
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
