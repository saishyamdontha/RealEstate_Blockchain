package com.example.RealEstate2.service;

import org.springframework.http.ResponseEntity;
import com.example.RealEstate2.model.User;


public interface LoginService {

    ResponseEntity<?> register(User user);

    ResponseEntity<?> login(String email, String password);

    ResponseEntity<?> logout();
}