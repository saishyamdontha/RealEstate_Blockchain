package com.example.RealEstate2.blockchain;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

public class ContractDeployer {
    public static void main(String[] args) throws Exception {
        String rpcUrl = System.getenv().getOrDefault("BLOCKCHAIN_RPC", "http://127.0.0.1:8545");
        String privateKey = System.getenv("BLOCKCHAIN_PRIVATE_KEY");
        if (privateKey == null) {
            throw new IllegalStateException("Set BLOCKCHAIN_PRIVATE_KEY env var before running this.");
        }

        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        Credentials credentials = Credentials.create(privateKey);
        StaticGasProvider gasProvider = new StaticGasProvider(
                BigInteger.valueOf(20_000_000_000L),
                BigInteger.valueOf(3_000_000L)
        );

        System.out.println("Deploying PropertyLedger to " + rpcUrl + " ...");
        PropertyLedger contract = PropertyLedger.deploy(web3j, credentials, gasProvider).send();
        System.out.println("Deployed successfully.");
        System.out.println("Contract address: " + contract.getContractAddress());
        web3j.shutdown();
    }
}
