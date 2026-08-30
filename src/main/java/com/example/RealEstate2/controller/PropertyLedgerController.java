package com.example.RealEstate2.controller;

import com.example.RealEstate2.service.PropertyLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/ledger")
@RequiredArgsConstructor
public class PropertyLedgerController {

    private final PropertyLedgerService ledgerService;

    @PostMapping("/register")
    public BigInteger registerProperty() throws Exception {
        return ledgerService.registerProperty();
    }

    @PostMapping("/{propertyId}/list")
    public String listProperty(
            @PathVariable Long propertyId,
            @RequestParam String buyerAddress,
            @RequestParam String lenderAddress,
            @RequestParam String inspectorAddress,
            @RequestParam BigInteger priceWei,
            @RequestParam BigInteger earnestAmountWei
    ) throws Exception {
        return ledgerService.listProperty(
                propertyId, buyerAddress, lenderAddress, inspectorAddress, priceWei, earnestAmountWei
        );
    }

    @PostMapping("/{propertyId}/earnest")
    public String depositEarnest(@PathVariable Long propertyId, @RequestParam BigInteger earnestAmountWei)
            throws Exception {
        return ledgerService.depositEarnest(propertyId, earnestAmountWei);
    }

    @PostMapping("/{propertyId}/inspection")
    public String updateInspectionStatus(@PathVariable Long propertyId, @RequestParam boolean passed)
            throws Exception {
        return ledgerService.updateInspectionStatus(propertyId, passed);
    }

    @PostMapping("/{propertyId}/fund")
    public String fundAsLender(@PathVariable Long propertyId, @RequestParam BigInteger remainingWei)
            throws Exception {
        return ledgerService.fundAsLender(propertyId, remainingWei);
    }

    @PostMapping("/{propertyId}/finalize")
    public String finalizeSale(@PathVariable Long propertyId) throws Exception {
        return ledgerService.finalizeSale(propertyId);
    }

    @PostMapping("/{propertyId}/cancel")
    public String cancelSale(@PathVariable Long propertyId) throws Exception {
        return ledgerService.cancelSale(propertyId);
    }

    @GetMapping("/{propertyId}")
    public PropertyLedgerService.SaleView getSale(@PathVariable Long propertyId) throws Exception {
        return ledgerService.getSale(propertyId);
    }

    @GetMapping("/{propertyId}/owner")
    public String getOwner(@PathVariable Long propertyId) throws Exception {
        return ledgerService.getPropertyOwner(propertyId);
    }

    @GetMapping("/next-id")
    public BigInteger getNextPropertyId() throws Exception {
        return ledgerService.getNextPropertyId();
    }
}
