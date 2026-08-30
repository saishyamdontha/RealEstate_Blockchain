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
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.CustomError;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple9;
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
    public static final String BINARY = "608060405234801561000f575f80fd5b50610f758061001d5f395ff3fe60806040526004361061008f575f3560e01c8063b5f522f711610057578063b5f522f714610121578063bd94b005146101ae578063d1fedbc2146101cd578063e740f77014610219578063eb5f22911461022c575f80fd5b8063358a5b321461009357806339500f34146100a85780635ed2c3db146100cf5780639d0e5fd6146100e3578063aa92938014610102575b5f80fd5b6100a66100a1366004610d71565b61024b565b005b3480156100b3575f80fd5b506100bc5f5481565b6040519081526020015b60405180910390f35b3480156100da575f80fd5b506100bc61039f565b3480156100ee575f80fd5b506100a66100fd366004610d88565b610400565b34801561010d575f80fd5b506100a661011c366004610d71565b61052e565b34801561012c575f80fd5b5061019961013b366004610d71565b600160208190525f91825260409091208054918101546002820154600383015460048401546005850154600686015460078701546008909701546001600160a01b039889169896871697958716969094169492939192909160ff1689565b6040516100c699989796959493929190610dee565b3480156101b9575f80fd5b506100a66101c8366004610d71565b6106fd565b3480156101d8575f80fd5b506102016101e7366004610d71565b60026020525f90815260409020546001600160a01b031681565b6040516001600160a01b0390911681526020016100c6565b6100a6610227366004610d71565b610a0e565b348015610237575f80fd5b506100a6610246366004610e6a565b610b5e565b5f8181526002602052604090205481906001600160a01b03166102895760405163245a964f60e21b8152600481018290526024015b60405180910390fd5b5f82815260016020526040902060028101546001600160a01b031633146102c557604051630527e73b60e31b8152336004820152602401610280565b6002600882015460ff1660058111156102e0576102e0610dba565b1461030a57600881015460405163e064752b60e01b81526102809160ff1690600290600401610ec5565b5f8160050154826004015461031f9190610efb565b905080341461034a57604051635928816d60e11b815234600482015260248101829052604401610280565b34826007015f82825461035d9190610f14565b9091555050604051348152339085907fcf171fe1914e77f855ef42bd62f7c012ac64337e37f46f358c854a214b9434029060200160405180910390a350505050565b5f805481806103ad83610f27565b909155505f8181526002602052604080822080546001600160a01b0319163390811790915590519293509183917ff16e2a0ca5373d3b3c5e53640bf2953ea25184f229ba20d421b5fc6a635e394591a390565b5f8281526002602052604090205482906001600160a01b03166104395760405163245a964f60e21b815260048101829052602401610280565b5f83815260016020526040902060038101546001600160a01b03163314610475576040516336b20e6160e11b8152336004820152602401610280565b6001600882015460ff16600581111561049057610490610dba565b146104ba57600881015460405163e064752b60e01b81526102809160ff1690600190600401610ec5565b826104c65760036104c9565b60025b60088201805460ff191660018360058111156104e7576104e7610dba565b0217905550837f6e254fae6dfb5e77180decd584a4dbc2bdbd358f9152749ea807aed85af1952784604051610520911515815260200190565b60405180910390a250505050565b5f8181526002602052604090205481906001600160a01b03166105675760405163245a964f60e21b815260048101829052602401610280565b5f8281526001602052604090206002600882015460ff16600581111561058f5761058f610dba565b146105b957600881015460405163e064752b60e01b81526102809160ff1690600290600401610ec5565b5f816007015482600601546105ce9190610f14565b905081600401548110156106015760048083015460405162fae2d560e21b81529182018390526024820152604401610280565b60088201805460ff1916600490811790915582546001840154918401545f8781526002602052604080822080546001600160a01b039687166001600160a01b0319909116811790915590519490931693849083908381818185875af1925050503d805f811461068b576040519150601f19603f3d011682016040523d82523d5f602084013e610690565b606091505b50509050806106b2576040516312171d8360e31b815260040160405180910390fd5b826001600160a01b0316846001600160a01b0316897f2231fc8bc2c20e26d85d66dc7f13a6b6d535c86d2a4d678bb2de61f5b713608360405160405180910390a45050505050505050565b5f8181526002602052604090205481906001600160a01b03166107365760405163245a964f60e21b815260048101829052602401610280565b5f828152600160208190526040909120908101546001600160a01b0316331480159061076c575080546001600160a01b03163314155b1561078c576040516303bb525960e21b8152336004820152602401610280565b6004600882015460ff1660058111156107a7576107a7610dba565b14806107cb57506005600882015460ff1660058111156107c9576107c9610dba565b145b156107f557600881015460405163e064752b60e01b81526102809160ff1690600190600401610ec5565b5f6002600883015460ff16600581111561081157610811610dba565b1490505f81610824578260060154610826565b5f5b90505f82610834575f61083a565b83600601545b60078501805460088701805460ff191660051790555f6006880181905590915590915082156108d85760018501546040515f916001600160a01b03169085908381818185875af1925050503d805f81146108af576040519150601f19603f3d011682016040523d82523d5f602084013e6108b4565b606091505b50509050806108d6576040516312171d8360e31b815260040160405180910390fd5b505b81156109505784546040515f916001600160a01b03169084908381818185875af1925050503d805f8114610927576040519150601f19603f3d011682016040523d82523d5f602084013e61092c565b606091505b505090508061094e576040516312171d8360e31b815260040160405180910390fd5b505b80156109cb5760028501546040515f916001600160a01b03169083908381818185875af1925050503d805f81146109a2576040519150601f19603f3d011682016040523d82523d5f602084013e6109a7565b606091505b50509050806109c9576040516312171d8360e31b815260040160405180910390fd5b505b6040518415158152339088907f057544975d58cdfbae539d30fdabb0b271612c30f7a9a0cdaa085de890290fcd906020015b60405180910390a350505050505050565b5f8181526002602052604090205481906001600160a01b0316610a475760405163245a964f60e21b815260048101829052602401610280565b5f828152600160208190526040909120908101546001600160a01b03163314610a85576040516303bb525960e21b8152336004820152602401610280565b5f600882015460ff166005811115610a9f57610a9f610dba565b14610ac857600881015460405163e064752b60e01b81526102809160ff16905f90600401610ec5565b80600501543414610afb576005810154604051635928816d60e11b81523460048201526024810191909152604401610280565b34816006015f828254610b0e9190610f14565b909155505060088101805460ff19166001179055604051348152339084907f7c6d91bd901e72cb2ba49f5796f5074c109048279c102fe97894cfe1891613c59060200160405180910390a3505050565b5f8681526002602052604090205486906001600160a01b0316610b975760405163245a964f60e21b815260048101829052602401610280565b5f878152600260205260409020546001600160a01b03163314610bcf576040516371c6811560e11b8152336004820152602401610280565b82821115610bfa57604051635928816d60e11b81526004810183905260248101849052604401610280565b604051806101200160405280336001600160a01b03168152602001876001600160a01b03168152602001866001600160a01b03168152602001856001600160a01b031681526020018481526020018381526020015f81526020015f81526020015f6005811115610c6c57610c6c610dba565b90525f88815260016020818152604092839020845181546001600160a01b03199081166001600160a01b039283161783559286015182850180548516918316919091179055938501516002820180548416918616919091179055606085015160038201805490931694169390931790556080830151600483015560a083015160058084019190915560c0840151600684015560e084015160078401556101008401516008840180549193909260ff19909216918490811115610d3057610d30610dba565b021790555050604080518581526020810185905233925089917f98868c46d49b200d82c273c9f28d73265826ca6567690262736450563dfff64c91016109fd565b5f60208284031215610d81575f80fd5b5035919050565b5f8060408385031215610d99575f80fd5b8235915060208301358015158114610daf575f80fd5b809150509250929050565b634e487b7160e01b5f52602160045260245ffd5b60068110610dea57634e487b7160e01b5f52602160045260245ffd5b9052565b6001600160a01b038a8116825289811660208301528881166040830152871660608201526080810186905260a0810185905260c0810184905260e081018390526101208101610e41610100830184610dce565b9a9950505050505050505050565b80356001600160a01b0381168114610e65575f80fd5b919050565b5f805f805f8060c08789031215610e7f575f80fd5b86359550610e8f60208801610e4f565b9450610e9d60408801610e4f565b9350610eab60608801610e4f565b92506080870135915060a087013590509295509295509295565b60408101610ed38285610dce565b610ee06020830184610dce565b9392505050565b634e487b7160e01b5f52601160045260245ffd5b81810381811115610f0e57610f0e610ee7565b92915050565b80820180821115610f0e57610f0e610ee7565b5f60018201610f3857610f38610ee7565b506001019056fea26469706673582212208b98c3f413ffa98477516617eeb32d8531e1d3b9c4c5631575780ef15873140b64736f6c63430008180033";

    private static String librariesLinkedBinary;

    public static final String FUNC_CANCELSALE = "cancelSale";

    public static final String FUNC_DEPOSITEARNEST = "depositEarnest";

    public static final String FUNC_FINALIZESALE = "finalizeSale";

    public static final String FUNC_FUNDASLENDER = "fundAsLender";

    public static final String FUNC_LISTPROPERTY = "listProperty";

    public static final String FUNC_NEXTPROPERTYID = "nextPropertyId";

    public static final String FUNC_PROPERTYOWNER = "propertyOwner";

    public static final String FUNC_REGISTERPROPERTY = "registerProperty";

    public static final String FUNC_SALES = "sales";

    public static final String FUNC_UPDATEINSPECTIONSTATUS = "updateInspectionStatus";

    public static final CustomError INCORRECTVALUE_ERROR = new CustomError("IncorrectValue", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final CustomError INSUFFICIENTFUNDS_ERROR = new CustomError("InsufficientFunds", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final CustomError NOTBUYER_ERROR = new CustomError("NotBuyer", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final CustomError NOTINSPECTOR_ERROR = new CustomError("NotInspector", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final CustomError NOTLENDER_ERROR = new CustomError("NotLender", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final CustomError NOTSELLER_ERROR = new CustomError("NotSeller", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final CustomError PROPERTYDOESNOTEXIST_ERROR = new CustomError("PropertyDoesNotExist", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    ;

    public static final CustomError TRANSFERFAILED_ERROR = new CustomError("TransferFailed", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError WRONGSTATUS_ERROR = new CustomError("WrongStatus", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Uint8>() {}));
    ;

    public static final Event EARNESTDEPOSITED_EVENT = new Event("EarnestDeposited", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event INSPECTIONUPDATED_EVENT = new Event("InspectionUpdated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Bool>() {}));
    ;

    public static final Event LENDERFUNDED_EVENT = new Event("LenderFunded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event LISTED_EVENT = new Event("Listed", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event PROPERTYREGISTERED_EVENT = new Event("PropertyRegistered", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event PROPERTYTRANSFERRED_EVENT = new Event("PropertyTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event SALECANCELLED_EVENT = new Event("SaleCancelled", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Bool>() {}));
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

    public static List<EarnestDepositedEventResponse> getEarnestDepositedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(EARNESTDEPOSITED_EVENT, transactionReceipt);
        ArrayList<EarnestDepositedEventResponse> responses = new ArrayList<EarnestDepositedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            EarnestDepositedEventResponse typedResponse = new EarnestDepositedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static EarnestDepositedEventResponse getEarnestDepositedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(EARNESTDEPOSITED_EVENT, log);
        EarnestDepositedEventResponse typedResponse = new EarnestDepositedEventResponse();
        typedResponse.log = log;
        typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<EarnestDepositedEventResponse> earnestDepositedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getEarnestDepositedEventFromLog(log));
    }

    public Flowable<EarnestDepositedEventResponse> earnestDepositedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(EARNESTDEPOSITED_EVENT));
        return earnestDepositedEventFlowable(filter);
    }

    public static List<InspectionUpdatedEventResponse> getInspectionUpdatedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(INSPECTIONUPDATED_EVENT, transactionReceipt);
        ArrayList<InspectionUpdatedEventResponse> responses = new ArrayList<InspectionUpdatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            InspectionUpdatedEventResponse typedResponse = new InspectionUpdatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.passed = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static InspectionUpdatedEventResponse getInspectionUpdatedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(INSPECTIONUPDATED_EVENT, log);
        InspectionUpdatedEventResponse typedResponse = new InspectionUpdatedEventResponse();
        typedResponse.log = log;
        typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.passed = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<InspectionUpdatedEventResponse> inspectionUpdatedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getInspectionUpdatedEventFromLog(log));
    }

    public Flowable<InspectionUpdatedEventResponse> inspectionUpdatedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(INSPECTIONUPDATED_EVENT));
        return inspectionUpdatedEventFlowable(filter);
    }

    public static List<LenderFundedEventResponse> getLenderFundedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(LENDERFUNDED_EVENT, transactionReceipt);
        ArrayList<LenderFundedEventResponse> responses = new ArrayList<LenderFundedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            LenderFundedEventResponse typedResponse = new LenderFundedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.lender = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static LenderFundedEventResponse getLenderFundedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(LENDERFUNDED_EVENT, log);
        LenderFundedEventResponse typedResponse = new LenderFundedEventResponse();
        typedResponse.log = log;
        typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.lender = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<LenderFundedEventResponse> lenderFundedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getLenderFundedEventFromLog(log));
    }

    public Flowable<LenderFundedEventResponse> lenderFundedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(LENDERFUNDED_EVENT));
        return lenderFundedEventFlowable(filter);
    }

    public static List<ListedEventResponse> getListedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(LISTED_EVENT, transactionReceipt);
        ArrayList<ListedEventResponse> responses = new ArrayList<ListedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ListedEventResponse typedResponse = new ListedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.seller = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.price = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.earnestAmount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ListedEventResponse getListedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(LISTED_EVENT, log);
        ListedEventResponse typedResponse = new ListedEventResponse();
        typedResponse.log = log;
        typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.seller = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.price = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.earnestAmount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<ListedEventResponse> listedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getListedEventFromLog(log));
    }

    public Flowable<ListedEventResponse> listedEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(LISTED_EVENT));
        return listedEventFlowable(filter);
    }

    public static List<PropertyRegisteredEventResponse> getPropertyRegisteredEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PROPERTYREGISTERED_EVENT, transactionReceipt);
        ArrayList<PropertyRegisteredEventResponse> responses = new ArrayList<PropertyRegisteredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PropertyRegisteredEventResponse typedResponse = new PropertyRegisteredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static PropertyRegisteredEventResponse getPropertyRegisteredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PROPERTYREGISTERED_EVENT, log);
        PropertyRegisteredEventResponse typedResponse = new PropertyRegisteredEventResponse();
        typedResponse.log = log;
        typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.owner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<PropertyRegisteredEventResponse> propertyRegisteredEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getPropertyRegisteredEventFromLog(log));
    }

    public Flowable<PropertyRegisteredEventResponse> propertyRegisteredEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PROPERTYREGISTERED_EVENT));
        return propertyRegisteredEventFlowable(filter);
    }

    public static List<PropertyTransferredEventResponse> getPropertyTransferredEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PROPERTYTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<PropertyTransferredEventResponse> responses = new ArrayList<PropertyTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PropertyTransferredEventResponse typedResponse = new PropertyTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.from = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.to = (String) eventValues.getIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static PropertyTransferredEventResponse getPropertyTransferredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PROPERTYTRANSFERRED_EVENT, log);
        PropertyTransferredEventResponse typedResponse = new PropertyTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.from = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.to = (String) eventValues.getIndexedValues().get(2).getValue();
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

    public static List<SaleCancelledEventResponse> getSaleCancelledEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(SALECANCELLED_EVENT, transactionReceipt);
        ArrayList<SaleCancelledEventResponse> responses = new ArrayList<SaleCancelledEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            SaleCancelledEventResponse typedResponse = new SaleCancelledEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.cancelledBy = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.earnestForfeited = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static SaleCancelledEventResponse getSaleCancelledEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(SALECANCELLED_EVENT, log);
        SaleCancelledEventResponse typedResponse = new SaleCancelledEventResponse();
        typedResponse.log = log;
        typedResponse.propertyId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.cancelledBy = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.earnestForfeited = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<SaleCancelledEventResponse> saleCancelledEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getSaleCancelledEventFromLog(log));
    }

    public Flowable<SaleCancelledEventResponse> saleCancelledEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(SALECANCELLED_EVENT));
        return saleCancelledEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> cancelSale(BigInteger propertyId) {
        final Function function = new Function(
                FUNC_CANCELSALE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(propertyId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> depositEarnest(BigInteger propertyId,
            BigInteger weiValue) {
        final Function function = new Function(
                FUNC_DEPOSITEARNEST, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(propertyId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> finalizeSale(BigInteger propertyId) {
        final Function function = new Function(
                FUNC_FINALIZESALE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(propertyId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> fundAsLender(BigInteger propertyId,
            BigInteger weiValue) {
        final Function function = new Function(
                FUNC_FUNDASLENDER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(propertyId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> listProperty(BigInteger propertyId, String buyer,
            String lender, String inspector, BigInteger price, BigInteger earnestAmount) {
        final Function function = new Function(
                FUNC_LISTPROPERTY, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(propertyId), 
                new org.web3j.abi.datatypes.Address(160, buyer), 
                new org.web3j.abi.datatypes.Address(160, lender), 
                new org.web3j.abi.datatypes.Address(160, inspector), 
                new org.web3j.abi.datatypes.generated.Uint256(price), 
                new org.web3j.abi.datatypes.generated.Uint256(earnestAmount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> nextPropertyId() {
        final Function function = new Function(FUNC_NEXTPROPERTYID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> propertyOwner(BigInteger param0) {
        final Function function = new Function(FUNC_PROPERTYOWNER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> registerProperty() {
        final Function function = new Function(
                FUNC_REGISTERPROPERTY, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple9<String, String, String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>> sales(
            BigInteger param0) {
        final Function function = new Function(FUNC_SALES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}));
        return new RemoteFunctionCall<Tuple9<String, String, String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>(function,
                new Callable<Tuple9<String, String, String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple9<String, String, String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> call(
                            ) throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple9<String, String, String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (String) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue(), 
                                (BigInteger) results.get(5).getValue(), 
                                (BigInteger) results.get(6).getValue(), 
                                (BigInteger) results.get(7).getValue(), 
                                (BigInteger) results.get(8).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> updateInspectionStatus(BigInteger propertyId,
            Boolean passed) {
        final Function function = new Function(
                FUNC_UPDATEINSPECTIONSTATUS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(propertyId), 
                new org.web3j.abi.datatypes.Bool(passed)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
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

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class EarnestDepositedEventResponse extends BaseEventResponse {
        public BigInteger propertyId;

        public String buyer;

        public BigInteger amount;
    }

    public static class InspectionUpdatedEventResponse extends BaseEventResponse {
        public BigInteger propertyId;

        public Boolean passed;
    }

    public static class LenderFundedEventResponse extends BaseEventResponse {
        public BigInteger propertyId;

        public String lender;

        public BigInteger amount;
    }

    public static class ListedEventResponse extends BaseEventResponse {
        public BigInteger propertyId;

        public String seller;

        public BigInteger price;

        public BigInteger earnestAmount;
    }

    public static class PropertyRegisteredEventResponse extends BaseEventResponse {
        public BigInteger propertyId;

        public String owner;
    }

    public static class PropertyTransferredEventResponse extends BaseEventResponse {
        public BigInteger propertyId;

        public String from;

        public String to;
    }

    public static class SaleCancelledEventResponse extends BaseEventResponse {
        public BigInteger propertyId;

        public String cancelledBy;

        public Boolean earnestForfeited;
    }
}
