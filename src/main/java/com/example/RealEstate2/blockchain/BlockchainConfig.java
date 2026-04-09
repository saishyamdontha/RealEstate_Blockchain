package com.example.RealEstate2.blockchain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;

@Configuration
public class BlockchainConfig {

    @Value("${blockchain.rpc}")
    private String rpcUrl;

    @Value("${blockchain.privateKey}")
    private String privateKey;

    @Value("${blockchain.contract}")
    private String contractAddress;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(rpcUrl));
    }

    @Bean
    public Credentials credentials() {
        return Credentials.create(privateKey);
    }

    // ✅ FIXES "exceeds block gas limit"
    @Bean
    public ContractGasProvider contractGasProvider() {
        return new ContractGasProvider() {

            @Override
            public BigInteger getGasPrice(String s) {
                return BigInteger.valueOf(20_000_000_000L); // 20 gwei
            }

            @Override
            public BigInteger getGasPrice() {
                return BigInteger.valueOf(20_000_000_000L);
            }

            @Override
            public BigInteger getGasLimit(String s) {
                return BigInteger.valueOf(6_000_000); // fits Ganache limit (6.7M)
            }

            @Override
            public BigInteger getGasLimit() {
                return BigInteger.valueOf(6_000_000);
            }
        };
    }

    @Bean
    public String contractAddress() {
        return contractAddress;
    }
}
