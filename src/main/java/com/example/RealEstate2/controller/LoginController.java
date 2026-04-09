package com.example.RealEstate2.controller;


import com.example.RealEstate2.model.User;
import com.example.RealEstate2.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private LoginService loginService;

    // REGISTER USER
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        return loginService.register(user);
    }

    // LOGIN USER
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        return loginService.login(user.getEmail(), user.getPassword());
    }

    // LOGOUT USER
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return loginService.logout();
    }
}