package com.example.RealEstate2.blockchain;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
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
 * <p>Generated with web3j version 1.7.0.
 */
@SuppressWarnings("rawtypes")
public class PropertyLedger extends Contract {
    public static final String BINARY = "0x608060405234801561001057600080fd5b50600436106100415760003560e01c80639377d7111461004657806398649f201461007a578063f28c2e2014610098575b600080fd5b610060600480360381019061005b91906103dd565b6100b4565b6040516100719594939291906104a9565b60405180910390f35b610082610292565b60405161008f9190610511565b60405180910390f35b6100b260048036038101906100ad9190610661565b61029e565b005b600081815481106100c457600080fd5b90600052602060002090600502016000915090508060000154908060010180546100ed9061074b565b80601f01602080910402602001604051908101604052809291908181526020018280546101199061074b565b80156101665780601f1061013b57610100808354040283529160200191610166565b820191906000526020600020905b81548152906001019060200180831161014957829003601f168201915b50505050509080600201805461017b9061074b565b80601f01602080910402602001604051908101604052809291908181526020018280546101a79061074b565b80156101f45780601f106101c9576101008083540402835291602001916101f4565b820191906000526020600020905b8154815290600101906020018083116101d757829003601f168201915b5050505050908060030180546102099061074b565b80601f01602080910402602001604051908101604052809291908181526020018280546102359061074b565b80156102825780601f1061025757610100808354040283529160200191610282565b820191906000526020600020905b81548152906001019060200180831161026557829003601f168201915b5050505050908060040154905085565b60008080549050905090565b60006040518060a0016040528086815260200185815260200184815260200183815260200142815250905060008190806001815401808255809150506001900390600052602060002090600502016000909190919091506000820151816000015560208201518160010190816103149190610928565b50604082015181600201908161032a9190610928565b5060608201518160030190816103409190610928565b506080820151816004015550507f2cb8b94a0e2cf9590bb66f7a3ed9415cd44b255dfa90adc8d81e0572a967bec385858585426040516103849594939291906104a9565b60405180910390a15050505050565b6000604051905090565b600080fd5b600080fd5b6000819050919050565b6103ba816103a7565b81146103c557600080fd5b50565b6000813590506103d7816103b1565b92915050565b6000602082840312156103f3576103f261039d565b5b6000610401848285016103c8565b91505092915050565b610413816103a7565b82525050565b600081519050919050565b600082825260208201905092915050565b60005b83811015610453578082015181840152602081019050610438565b60008484015250505050565b6000601f19601f8301169050919050565b600061047b82610419565b6104858185610424565b9350610495818560208601610435565b61049e8161045f565b840191505092915050565b600060a0820190506104be600083018861040a565b81810360208301526104d08187610470565b905081810360408301526104e48186610470565b905081810360608301526104f88185610470565b9050610507608083018461040a565b9695505050505050565b6000602082019050610526600083018461040a565b92915050565b600080fd5b600080fd5b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b61056e8261045f565b810181811067ffffffffffffffff8211171561058d5761058c610536565b5b80604052505050565b60006105a0610393565b90506105ac8282610565565b919050565b600067ffffffffffffffff8211156105cc576105cb610536565b5b6105d58261045f565b9050602081019050919050565b82818337600083830152505050565b60006106046105ff846105b1565b610596565b9050828152602081018484840111156106205761061f610531565b5b61062b8482856105e2565b509392505050565b600082601f8301126106485761064761052c565b5b81356106588482602086016105f1565b91505092915050565b6000806000806080858703121561067b5761067a61039d565b5b6000610689878288016103c8565b945050602085013567ffffffffffffffff8111156106aa576106a96103a2565b5b6106b687828801610633565b935050604085013567ffffffffffffffff8111156106d7576106d66103a2565b5b6106e387828801610633565b925050606085013567ffffffffffffffff811115610704576107036103a2565b5b61071087828801610633565b91505092959194509250565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052602260045260246000fd5b6000600282049050600182168061076357607f821691505b6020821081036107765761077561071c565b5b50919050565b60008190508160005260206000209050919050565b60006020601f8301049050919050565b600082821b905092915050565b6000600883026107de7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff826107a1565b6107e886836107a1565b95508019841693508086168417925050509392505050565b6000819050919050565b600061082561082061081b846103a7565b610800565b6103a7565b9050919050565b6000819050919050565b61083f8361080a565b61085361084b8261082c565b8484546107ae565b825550505050565b600090565b61086861085b565b610873818484610836565b505050565b5b818110156108975761088c600082610860565b600181019050610879565b5050565b601f8211156108dc576108ad8161077c565b6108b684610791565b810160208510156108c5578190505b6108d96108d185610791565b830182610878565b50505b505050565b600082821c905092915050565b60006108ff600019846008026108e1565b1980831691505092915050565b600061091883836108ee565b9150826002028217905092915050565b61093182610419565b67ffffffffffffffff81111561094a57610949610536565b5b610954825461074b565b61095f82828561089b565b600060209050601f8311600181146109925760008415610980578287015190505b61098a858261090c565b8655506109f2565b601f1984166109a08661077c565b60005b828110156109c8578489015182556001820191506020850194506020810190506109a3565b868310156109e557848901516109e1601f8916826108ee565b8355505b6001600288020188555050505b50505050505056fea2646970667358221220ebd157d13a4109a2147426873a9813772c55a1c9efb79873b3a95e877db0ae3064736f6c63430008140033";

//    private static String librariesLinkedBinary;

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

    public RemoteFunctionCall<TransactionReceipt> recordTransfer(BigInteger _propertyId,
                                                                 String _sellerId, String _buyerId, String _txHash) {
        final Function function = new Function(
                FUNC_RECORDTRANSFER,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_propertyId),
                        new org.web3j.abi.datatypes.Utf8String(_sellerId),
                        new org.web3j.abi.datatypes.Utf8String(_buyerId),
                        new org.web3j.abi.datatypes.Utf8String(_txHash)),
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

//    public static void linkLibraries(List<Contract.LinkReference> references) {
//        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
//    }

//    private static String getDeploymentBinary() {
//        if (librariesLinkedBinary != null) {
//            return librariesLinkedBinary;
//        } else {
//            return BINARY;
//        }
//    }

    private static String getDeploymentBinary() {
        return BINARY;
    }

    public static class PropertyTransferredEventResponse extends BaseEventResponse {
        public BigInteger propertyId;

        public String sellerId;

        public String buyerId;

        public String txHash;

        public BigInteger timestamp;
    }
}
