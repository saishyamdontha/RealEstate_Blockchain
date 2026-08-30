package com.example.RealEstate2.service;

import com.example.RealEstate2.model.Property;
import com.example.RealEstate2.model.User;
import com.example.RealEstate2.repository.PropertyRepository;
import com.example.RealEstate2.repository.TransactionRepository;
import com.example.RealEstate2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private PropertyLedgerService ledgerService;
    @Mock private UserRepository userRepository;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    private User user;
    private Property property;

    @BeforeEach
    void setUp() {
        user = new User();
        // id is auto-generated in real usage; for mocking findById we just
        // need a non-null User to return.

        property = new Property();
        property.setPropertyId("PROP-001");
        property.setAddress("123 Test St");
        property.setLatitude("12.34");
        property.setLongitude("56.78");
        property.setTitleDeedNumber("TD-001");
    }

    // -----------------------------------------------------
    // getPropertyUser: on-chain registration failure must
    // block the DB save entirely -- no orphaned DB-only rows.
    // -----------------------------------------------------
    @Test
    void getPropertyUser_doesNotSaveToDb_whenOnChainRegistrationFails() throws Exception {
        when(propertyRepository.existsByPropertyId(any())).thenReturn(false);
        when(propertyRepository.existsByAddress(any())).thenReturn(false);
        when(propertyRepository.existsByLatitudeAndLongitude(any(), any())).thenReturn(false);
        when(propertyRepository.existsByTitleDeedNumber(any())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ledgerService.registerProperty()).thenThrow(new RuntimeException("RPC unreachable"));

        ResponseEntity<?> response = propertyService.getPropertyUser(property, "user-001", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(propertyRepository, never()).save(any());
    }

    // -----------------------------------------------------
    // getPropertyUser: successful registration must persist
    // the returned on-chain ID onto the saved Property.
    // -----------------------------------------------------
    @Test
    void getPropertyUser_setsBlockchainPropertyId_whenRegistrationSucceeds() throws Exception {
        when(propertyRepository.existsByPropertyId(any())).thenReturn(false);
        when(propertyRepository.existsByAddress(any())).thenReturn(false);
        when(propertyRepository.existsByLatitudeAndLongitude(any(), any())).thenReturn(false);
        when(propertyRepository.existsByTitleDeedNumber(any())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ledgerService.registerProperty()).thenReturn(BigInteger.valueOf(7));

        ResponseEntity<?> response = propertyService.getPropertyUser(property, "user-001", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(property.getBlockchainPropertyId()).isEqualTo(7L);
        verify(propertyRepository, times(1)).save(property);
    }

    // -----------------------------------------------------
    // getPropertyUser: a property matching existing criteria
    // must be rejected before any blockchain call is attempted.
    // -----------------------------------------------------
    @Test
    void getPropertyUser_rejectsDuplicate_withoutCallingBlockchain() {
        when(propertyRepository.existsByPropertyId(any())).thenReturn(true);

        ResponseEntity<?> response = propertyService.getPropertyUser(property, "user-001", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(ledgerService);
    }

    // -----------------------------------------------------
    // deleteProperty: must refuse deletion while a property
    // is actively listed for sale (possible in-progress escrow).
    // -----------------------------------------------------
    @Test
    void deleteProperty_refusesDeletion_whenPropertyIsForSale() {
        property.setForSale(true);
        property.setUser(userWithId(1L));
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));

        ResponseEntity<?> response = propertyService.deleteProperty(1L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(propertyRepository, never()).delete(any());
    }

    // -----------------------------------------------------
    // deleteProperty: allowed once the property is not listed.
    // -----------------------------------------------------
    @Test
    void deleteProperty_succeeds_whenPropertyIsNotForSale() {
        property.setForSale(false);
        property.setUser(userWithId(1L));
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));

        ResponseEntity<?> response = propertyService.deleteProperty(1L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(propertyRepository, times(1)).delete(property);
    }

    // -----------------------------------------------------
    // transferProperty: must refuse to finalize a sale for a
    // property that was never registered on-chain -- prevents
    // the DB-primary-key-as-blockchain-ID bug this test guards
    // against regressing.
    // -----------------------------------------------------
    @Test
    void transferProperty_throwsIllegalStateException_whenBlockchainPropertyIdIsNull() {
        property.setUniqueUserId("seller-001");
        property.setBlockchainPropertyId(null);
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(userRepository.findByUniqueId("buyer-001")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> propertyService.transferProperty(1L, "seller-001", "buyer-001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has not been registered on-chain");

        verifyNoInteractions(ledgerService);
    }

    private User userWithId(long id) {
        // User.id has no public setter (JPA @GeneratedValue) -- set it
        // via reflection for test purposes, the standard approach for
        // entities without an ID setter.
        User u = new User();
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }
}
