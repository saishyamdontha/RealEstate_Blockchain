package com.example.RealEstate2.blockchain;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.8.0.
 */
@SuppressWarnings("rawtypes")
@Generated("org.web3j.codegen.SolidityFunctionWrapperGenerator")
public class PropertyLedger extends Contract {
    public static final String BINARY = "608060405234801561001057600080fd5b50610a2a806100206000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c80639377d7111461004657806398649f201461007a578063f28c2e2014610098575b600080fd5b610060600480360381019061005b91906103d7565b6100b4565b6040516100719594939291906104a3565b60405180910390f35b610082610292565b60405161008f919061050b565b60405180910390f35b6100b260048036038101906100ad919061065b565b61029e565b005b600081815481106100c457600080fd5b90600052602060002090600502016000915090508060000154908060010180546100ed90610745565b80601f016020809104026020016040519081016040528092919081815260200182805461011990610745565b80156101665780601f1061013b57610100808354040283529160200191610166565b820191906000526020600020905b81548152906001019060200180831161014957829003601f168201915b50505050509080600201805461017b90610745565b80601f01602080910402602001604051908101604052809291908181526020018280546101a790610745565b80156101f45780601f106101c9576101008083540402835291602001916101f4565b820191906000526020600020905b8154815290600101906020018083116101d757829003601f168201915b50505050509080600301805461020990610745565b80601f016020809104026020016040519081016040528092919081815260200182805461023590610745565b80156102825780601f1061025757610100808354040283529160200191610282565b820191906000526020600020905b81548152906001019060200180831161026557829003601f168201915b5050505050908060040154905085565b60008080549050905090565b60006040518060a0016040528086815260200185815260200184815260200183815260200142815250908060018154018082558091505060019003906000526020600020906005020160009091909190915060008201518160000155602082015181600101908161030f9190610922565b5060408201518160020190816103259190610922565b50606082015181600301908161033b9190610922565b506080820151816004015550507f2cb8b94a0e2cf9590bb66f7a3ed9415cd44b255dfa90adc8d81e0572a967bec3848484844260405161037f9594939291906104a3565b60405180910390a150505050565b6000604051905090565b600080fd5b600080fd5b6000819050919050565b6103b4816103a1565b81146103bf57600080fd5b50565b6000813590506103d1816103ab565b92915050565b6000602082840312156103ed576103ec610397565b5b60006103fb848285016103c2565b91505092915050565b61040d816103a1565b82525050565b600081519050919050565b600082825260208201905092915050565b60005b8381101561044d578082015181840152602081019050610432565b60008484015250505050565b6000601f19601f8301169050919050565b600061047582610413565b61047f818561041e565b935061048f81856020860161042f565b61049881610459565b840191505092915050565b600060a0820190506104b86000830188610404565b81810360208301526104ca818761046a565b905081810360408301526104de818661046a565b905081810360608301526104f2818561046a565b90506105016080830184610404565b9695505050505050565b60006020820190506105206000830184610404565b92915050565b600080fd5b600080fd5b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b61056882610459565b810181811067ffffffffffffffff8211171561058757610586610530565b5b80604052505050565b600061059a61038d565b90506105a6828261055f565b919050565b600067ffffffffffffffff8211156105c6576105c5610530565b5b6105cf82610459565b9050602081019050919050565b82818337600083830152505050565b60006105fe6105f9846105ab565b610590565b90508281526020810184848401111561061a5761061961052b565b5b6106258482856105dc565b509392505050565b600082601f83011261064257610641610526565b5b81356106528482602086016105eb565b91505092915050565b6000806000806080858703121561067557610674610397565b5b6000610683878288016103c2565b945050602085013567ffffffffffffffff8111156106a4576106a361039c565b5b6106b08782880161062d565b935050604085013567ffffffffffffffff8111156106d1576106d061039c565b5b6106dd8782880161062d565b925050606085013567ffffffffffffffff8111156106fe576106fd61039c565b5b61070a8782880161062d565b91505092959194509250565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052602260045260246000fd5b6000600282049050600182168061075d57607f821691505b6020821081036107705761076f610716565b5b50919050565b60008190508160005260206000209050919050565b60006020601f8301049050919050565b600082821b905092915050565b6000600883026107d87fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8261079b565b6107e2868361079b565b95508019841693508086168417925050509392505050565b6000819050919050565b600061081f61081a610815846103a1565b6107fa565b6103a1565b9050919050565b6000819050919050565b61083983610804565b61084d61084582610826565b8484546107a8565b825550505050565b600090565b610862610855565b61086d818484610830565b505050565b5b818110156108915761088660008261085a565b600181019050610873565b5050565b601f8211156108d6576108a781610776565b6108b08461078b565b810160208510156108bf578190505b6108d36108cb8561078b565b830182610872565b50505b505050565b600082821c905092915050565b60006108f9600019846008026108db565b1980831691505092915050565b600061091283836108e8565b9150826002028217905092915050565b61092b82610413565b67ffffffffffffffff81111561094457610943610530565b5b61094e8254610745565b610959828285610895565b600060209050601f83116001811461098c576000841561097a578287015190505b6109848582610906565b8655506109ec565b601f19841661099a86610776565b60005b828110156109c25784890151825560018201915060208501945060208101905061099d565b868310156109df57848901516109db601f8916826108e8565b8355505b6001600288020188555050505b50505050505056fea26469706673582212207fe4cdefc3ac832f4933ae53a4bde7eb06d1f3aed96fe46e1ec92152982da50864736f6c63430008130033";

    private static String librariesLinkedBinary;

    public static final String FUNC_GETTRANSFERSCOUNT = "getTransfersCount";

    public static final String FUNC_RECORDTRANSFER = "recordTransfer";

    public static final String FUNC_TRANSFERS = "transfers";

    public static final Event PROPERTYTRANSFERRED_EVENT = new Event("PropertyTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected PropertyLedger(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected PropertyLedger(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected PropertyLedger(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected PropertyLedger(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<PropertyTransferredEventResponse> getPropertyTransferredEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PROPERTYTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<PropertyTransferredEventResponse> responses = new ArrayList<PropertyTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PropertyTransferredEventResponse typedResponse = new PropertyTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.propertyId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.sellerId = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.buyerId = (String) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.txHash = (String) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static PropertyTransferredEventResponse getPropertyTransferredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PROPERTYTRANSFERRED_EVENT, log);
        PropertyTransferredEventResponse typedResponse = new PropertyTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.propertyId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.sellerId = (String) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.buyerId = (String) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.txHash = (String) eventValues.getNonIndexedValues().get(3).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
        return typedResponse;
    }

    public Flowable<PropertyTransferredEventResponse> propertyTransferredEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getPropertyTransferredEventFromLog(log));
    }

    public Flowable<PropertyTransferredEventResponse> propertyTransferredEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PROPERTYTRANSFERRED_EVENT));
        return propertyTransferredEventFlowable(filter);
    }

    public RemoteFunctionCall<BigInteger> getTransfersCount() {
        final Function function = new Function(FUNC_GETTRANSFERSCOUNT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> recordTransfer(BigInteger propertyId,
            String sellerId, String buyerId, String txHash) {
        final Function function = new Function(
                FUNC_RECORDTRANSFER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(propertyId), 
                new org.web3j.abi.datatypes.Utf8String(sellerId), 
                new org.web3j.abi.datatypes.Utf8String(buyerId), 
                new org.web3j.abi.datatypes.Utf8String(txHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple5<BigInteger, String, String, String, BigInteger>> transfers(
            BigInteger param0) {
        final Function function = new Function(FUNC_TRANSFERS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple5<BigInteger, String, String, String, BigInteger>>(function,
                new Callable<Tuple5<BigInteger, String, String, String, BigInteger>>() {
                    @Override
                    public Tuple5<BigInteger, String, String, String, BigInteger> call() throws
                            Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple5<BigInteger, String, String, String, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (String) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue());
                    }
                });
    }

    @Deprecated
    public static PropertyLedger load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new PropertyLedger(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static PropertyLedger load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new PropertyLedger(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static PropertyLedger load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new PropertyLedger(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static PropertyLedger load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new PropertyLedger(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<PropertyLedger> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(PropertyLedger.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<PropertyLedger> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(PropertyLedger.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static RemoteCall<PropertyLedger> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(PropertyLedger.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<PropertyLedger> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(PropertyLedger.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }


    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class PropertyTransferredEventResponse extends BaseEventResponse {
        public BigInteger propertyId;

        public String sellerId;

        public String buyerId;

        public String txHash;

        public BigInteger timestamp;
    }
}
