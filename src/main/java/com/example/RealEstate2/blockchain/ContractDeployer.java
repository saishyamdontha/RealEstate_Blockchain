package com.example.RealEstate2.blockchain;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

public class ContractDeployer {

    public static void main(String[] args) throws Exception {
        String rpcUrl = "http://127.0.0.1:8545";
        String privateKey = "ac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";

        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        Credentials credentials = Credentials.create(privateKey);

        StaticGasProvider gasProvider = new StaticGasProvider(
                BigInteger.valueOf(20_000_000_000L),
                BigInteger.valueOf(6_000_000L)
        );

        System.out.println("Deploying PropertyLedger to " + rpcUrl + " ...");
        PropertyLedger contract = PropertyLedger.deploy(web3j, credentials, gasProvider).send();
        System.out.println("Deployed successfully.");
        System.out.println("Contract address: " + contract.getContractAddress());

        web3j.shutdown();
    }
}
