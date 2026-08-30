package com.example.RealEstate2.blockchain;

public class ContractRevertException extends RuntimeException {
    private final String errorName;
    private final Object[] args;

    public ContractRevertException(String errorName, Object[] args) {
        super("Contract reverted: " + errorName + " " + java.util.Arrays.toString(args));
        this.errorName = errorName;
        this.args = args;
    }

    public String getErrorName() {
        return errorName;
    }

    public Object[] getArgs() {
        return args;
    }
}
