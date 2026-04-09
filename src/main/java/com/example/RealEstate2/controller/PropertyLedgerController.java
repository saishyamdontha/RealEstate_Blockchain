package com.example.RealEstate2.controller;

import com.example.RealEstate2.service.PropertyLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ledger")
@RequiredArgsConstructor
public class PropertyLedgerController {

    private final PropertyLedgerService ledgerService;

    @PostMapping("/record")
    public String recordTransfer(@RequestParam Long propertyId,
                                 @RequestParam String sellerId,
                                 @RequestParam String buyerId,
                                 @RequestParam String offchainTxHash) throws Exception {

        return ledgerService.recordTransfer(propertyId, sellerId, buyerId, offchainTxHash);
    }

    @GetMapping("/count")
    public Long getCount() throws Exception {
        return ledgerService.getTransfersCount();
    }
}
