package com.example.RealEstate2.service;

import com.example.RealEstate2.blockchain.PropertyLedger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class PropertyLedgerService {

    private final Web3j web3j;
    private final Credentials credentials;

    private PropertyLedger loadContract() {
        return PropertyLedger.load(
                System.getenv("CONTRACT_ADDRESS"),
                web3j,
                credentials,
                new DefaultGasProvider()
        );
    }

    public String recordTransfer(Long propertyId, String sellerId, String buyerId, String offchainTxHash) throws Exception {
        PropertyLedger contract = loadContract();

        var txReceipt = contract.recordTransfer(
                BigInteger.valueOf(propertyId),
                sellerId,
                buyerId,
                offchainTxHash
        ).send();

        return txReceipt.getTransactionHash();
    }

    public Long getTransfersCount() throws Exception {
        PropertyLedger contract = loadContract();
        return contract.getTransfersCount().send().longValue();
    }
}
