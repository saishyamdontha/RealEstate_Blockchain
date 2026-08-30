package com.example.RealEstate2.controller.advice;

import com.example.RealEstate2.blockchain.ContractRevertException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ContractErrorHandler {

    @ExceptionHandler(ContractRevertException.class)
    public ResponseEntity<Map<String, Object>> handleContractRevert(ContractRevertException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", ex.getErrorName(),
                "args", ex.getArgs(),
                "message", ex.getMessage()
        ));
    }
}
