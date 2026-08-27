package com.example.RealEstate2.service;

import com.example.RealEstate2.blockchain.PropertyLedger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class PropertyLedgerService {

    private final Web3j web3j;
    private final Credentials credentials;
    private final String contractAddress;

    // Writes (recordTransfer) need real signing -> Credentials + RawTransactionManager (web3j's default for this overload)
    private PropertyLedger loadWritableContract() {
        return PropertyLedger.load(
                contractAddress,
                web3j,
                credentials,
                new DefaultGasProvider()
        );
    }

    // Reads (getTransfersCount) are plain eth_call, no signing needed -> ReadonlyTransactionManager
    private PropertyLedger loadReadOnlyContract() {
        ReadonlyTransactionManager txManager = new ReadonlyTransactionManager(web3j, contractAddress);
        return PropertyLedger.load(
                contractAddress,
                web3j,
                txManager,
                new DefaultGasProvider()
        );
    }

    public String recordTransfer(Long propertyId, String sellerId, String buyerId, String offchainTxHash) throws Exception {
        PropertyLedger contract = loadWritableContract();

        var txReceipt = contract.recordTransfer(
                BigInteger.valueOf(propertyId),
                sellerId,
                buyerId,
                offchainTxHash
        ).send();

        return txReceipt.getTransactionHash();
    }

    public Long getTransfersCount() throws Exception {
        PropertyLedger contract = loadReadOnlyContract();
        return contract.getTransfersCount().send().longValue();
    }
}
