package com.example.RealEstate2.blockchain;

import org.web3j.abi.CustomErrorEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.CustomError;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.FunctionReturnDecoder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Decodes a raw ABI-encoded revert payload against a contract wrapper's
 * generated CustomError constants (e.g. PropertyLedger.NOTBUYER_ERROR).
 *
 * web3j 5.0.2 generates CustomError definitions in the wrapper class but
 * does NOT auto-decode revert data against them anywhere in the send()
 * path -- TransactionException only ever carries the node's generic
 * revert string. This class closes that gap manually, built by reflecting
 * over the wrapper's public static CustomError fields so it stays correct
 * if the contract (and therefore the generated wrapper) changes.
 */
public class ContractRevertDecoder {

    private final Map<String, CustomError> selectorToError = new HashMap<>();

    public ContractRevertDecoder(Class<?> contractWrapperClass) {
        for (Field field : contractWrapperClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (!CustomError.class.isAssignableFrom(field.getType())) continue;
            try {
                CustomError error = (CustomError) field.get(null);
                String selector = CustomErrorEncoder.encode(error).substring(0, 10);
                selectorToError.put(selector, error);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Could not read CustomError field " + field.getName(), e);
            }
        }
    }

    /**
     * @param revertDataHex raw hex string from Response.Error.getData(),
     *                       e.g. "0x30cd7471000000000000000000000000...".
     * @return a human-readable decoded form, or null if this data doesn't
     *         match any known custom error on this contract (e.g. it's a
     *         standard Error(string) revert instead, or unrecognized data).
     */
    public DecodedRevert decode(String revertDataHex) {
        // web3j 5.0.2 / Ganache: Response.Error.getData() has been observed
        // to return the JSON string value WITH its enclosing quote
        // characters still attached (e.g. literal "0x1234..." instead of
        // 0x1234...). Strip any stray leading/trailing quotes before using
        // it -- confirmed via byte-level inspection during development;
        // not documented anywhere, found empirically.
        if (revertDataHex != null) {
            revertDataHex = revertDataHex.trim();
            if (revertDataHex.startsWith("\"")) revertDataHex = revertDataHex.substring(1);
            if (revertDataHex.endsWith("\"")) revertDataHex = revertDataHex.substring(0, revertDataHex.length() - 1);
        }

        if (revertDataHex == null || revertDataHex.length() < 10) {
            return null;
        }
        String selector = revertDataHex.substring(0, 10);
        CustomError error = selectorToError.get(selector);
        if (error == null) {
            return null;
        }

        try {
            String argsHex = "0x" + revertDataHex.substring(10);
            List<TypeReference<Type>> outputParams = error.getParameters().stream()
                    .map(tr -> (TypeReference<Type>) tr)
                    .collect(Collectors.toList());
            List<Type> decoded = FunctionReturnDecoder.decode(argsHex, outputParams);

            List<Object> values = decoded.stream().map(Type::getValue).collect(Collectors.toList());
            return new DecodedRevert(error.getName(), values);
        } catch (Exception e) {
            // Malformed or unexpected data for this selector -- treat as
            // undecodable rather than propagating a decoder-internal
            // exception up through the caller's control flow.
            return null;
        }
    }

    public record DecodedRevert(String errorName, List<Object> args) {
        @Override
        public String toString() {
            return errorName + args;
        }
    }
}
