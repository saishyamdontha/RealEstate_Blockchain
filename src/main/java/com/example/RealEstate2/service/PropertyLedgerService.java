package com.example.RealEstate2.service;

import com.example.RealEstate2.blockchain.ContractRevertDecoder;
import com.example.RealEstate2.blockchain.ContractRevertException;
import com.example.RealEstate2.blockchain.PropertyLedger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class PropertyLedgerService {

    private final Web3j web3j;
    private final Credentials credentials;
    private final String contractAddress;
    private final ContractGasProvider contractGasProvider;

    private final ContractRevertDecoder revertDecoder = new ContractRevertDecoder(PropertyLedger.class);

    // NOTE: single backend-held Credentials signs every transaction below,
    // regardless of which "role" (seller/buyer/lender/inspector) is calling.
    // This is a demo simplification, not production Web3 architecture -- see
    // README for the tradeoff. A production version would have the frontend
    // sign with each user's own wallet and this service would only read
    // on-chain state, not submit transactions on anyone's behalf.
    private PropertyLedger loadWritableContract() {
        return PropertyLedger.load(contractAddress, web3j, credentials, contractGasProvider);
    }

    private PropertyLedger loadReadOnlyContract() {
        ReadonlyTransactionManager txManager = new ReadonlyTransactionManager(web3j, contractAddress);
        return PropertyLedger.load(contractAddress, web3j, txManager, contractGasProvider);
    }

    /**
     * Replays {@code function} as a read-only eth_call before it's actually
     * sent as a transaction. If the node returns revert data, decodes it
     * against the contract's known custom errors and throws
     * ContractRevertException with the real error name and arguments --
     * instead of letting the real transaction fail on-chain (costing real
     * gas) and getting back web3j's generic TransactionException message,
     * which is all send() ever surfaces (see ContractRevertDecoder javadoc).
     *
     * If the node doesn't return decodable data, this silently does nothing
     * and the real transaction proceeds as before -- this is a best-effort
     * improvement, not a guarantee.
     */
    private void simulateOrThrow(Function function) throws Exception {
        String encodedFunction = FunctionEncoder.encode(function);
        String fromAddress = credentials.getAddress();

        EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(fromAddress, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
        ).send();

        if (response.hasError()) {
            String data = response.getError().getData();
            ContractRevertDecoder.DecodedRevert decoded = revertDecoder.decode(data);
            if (decoded != null) {
                throw new ContractRevertException(decoded.errorName(), decoded.args().toArray());
            }
        }
    }

    public BigInteger registerProperty() throws Exception {
        PropertyLedger contract = loadWritableContract();
        var receipt = contract.registerProperty().send();
        var events = PropertyLedger.getPropertyRegisteredEvents(receipt);
        if (events.isEmpty()) {
            throw new IllegalStateException("registerProperty succeeded but no PropertyRegistered event was found");
        }
        return events.get(0).propertyId;
    }

    public String listProperty(
            Long propertyId,
            String buyerAddress,
            String lenderAddress,
            String inspectorAddress,
            BigInteger priceWei,
            BigInteger earnestAmountWei
    ) throws Exception {
        Function function = new Function(
                PropertyLedger.FUNC_LISTPROPERTY,
                Arrays.asList(
                        new Uint256(BigInteger.valueOf(propertyId)),
                        new Address(buyerAddress),
                        new Address(lenderAddress),
                        new Address(inspectorAddress),
                        new Uint256(priceWei),
                        new Uint256(earnestAmountWei)
                ),
                Collections.<TypeReference<?>>emptyList()
        );
        simulateOrThrow(function);

        PropertyLedger contract = loadWritableContract();
        var receipt = contract.listProperty(
                BigInteger.valueOf(propertyId), buyerAddress, lenderAddress, inspectorAddress, priceWei, earnestAmountWei
        ).send();
        return receipt.getTransactionHash();
    }

    public String depositEarnest(Long propertyId, BigInteger earnestAmountWei) throws Exception {
        Function function = new Function(
                PropertyLedger.FUNC_DEPOSITEARNEST,
                Arrays.<Type>asList(new Uint256(BigInteger.valueOf(propertyId))),
                Collections.<TypeReference<?>>emptyList()
        );
        simulateOrThrow(function);

        PropertyLedger contract = loadWritableContract();
        var receipt = contract.depositEarnest(BigInteger.valueOf(propertyId), earnestAmountWei).send();
        return receipt.getTransactionHash();
    }

    public String updateInspectionStatus(Long propertyId, boolean passed) throws Exception {
        Function function = new Function(
                PropertyLedger.FUNC_UPDATEINSPECTIONSTATUS,
                Arrays.<Type>asList(new Uint256(BigInteger.valueOf(propertyId)), new Bool(passed)),
                Collections.<TypeReference<?>>emptyList()
        );
        simulateOrThrow(function);

        PropertyLedger contract = loadWritableContract();
        var receipt = contract.updateInspectionStatus(BigInteger.valueOf(propertyId), passed).send();
        return receipt.getTransactionHash();
    }

    public String fundAsLender(Long propertyId, BigInteger remainingWei) throws Exception {
        Function function = new Function(
                PropertyLedger.FUNC_FUNDASLENDER,
                Arrays.<Type>asList(new Uint256(BigInteger.valueOf(propertyId))),
                Collections.<TypeReference<?>>emptyList()
        );
        simulateOrThrow(function);

        PropertyLedger contract = loadWritableContract();
        var receipt = contract.fundAsLender(BigInteger.valueOf(propertyId), remainingWei).send();
        return receipt.getTransactionHash();
    }

    public String finalizeSale(Long propertyId) throws Exception {
        Function function = new Function(
                PropertyLedger.FUNC_FINALIZESALE,
                Arrays.<Type>asList(new Uint256(BigInteger.valueOf(propertyId))),
                Collections.<TypeReference<?>>emptyList()
        );
        simulateOrThrow(function);

        PropertyLedger contract = loadWritableContract();
        var receipt = contract.finalizeSale(BigInteger.valueOf(propertyId)).send();
        return receipt.getTransactionHash();
    }

    public String cancelSale(Long propertyId) throws Exception {
        Function function = new Function(
                PropertyLedger.FUNC_CANCELSALE,
                Arrays.<Type>asList(new Uint256(BigInteger.valueOf(propertyId))),
                Collections.<TypeReference<?>>emptyList()
        );
        simulateOrThrow(function);

        PropertyLedger contract = loadWritableContract();
        var receipt = contract.cancelSale(BigInteger.valueOf(propertyId)).send();
        return receipt.getTransactionHash();
    }

    public SaleView getSale(Long propertyId) throws Exception {
        PropertyLedger contract = loadReadOnlyContract();
        var tuple = contract.sales(BigInteger.valueOf(propertyId)).send();
        return new SaleView(
                tuple.component1(),
                tuple.component2(),
                tuple.component3(),
                tuple.component4(),
                tuple.component5(),
                tuple.component6(),
                tuple.component7(),
                tuple.component8(),
                tuple.component9().intValue()
        );
    }

    public String getPropertyOwner(Long propertyId) throws Exception {
        PropertyLedger contract = loadReadOnlyContract();
        return contract.propertyOwner(BigInteger.valueOf(propertyId)).send();
    }

    public BigInteger getNextPropertyId() throws Exception {
        PropertyLedger contract = loadReadOnlyContract();
        return contract.nextPropertyId().send();
    }

    public record SaleView(
            String seller,
            String buyer,
            String lender,
            String inspector,
            BigInteger price,
            BigInteger earnestAmount,
            BigInteger depositedByBuyer,
            BigInteger depositedByLender,
            int status
    ) {}
}
