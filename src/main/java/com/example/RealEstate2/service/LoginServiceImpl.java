package com.example.RealEstate2.service;


import com.example.RealEstate2.model.User;
import com.example.RealEstate2.model.status.UserStatus;
import com.example.RealEstate2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public ResponseEntity<?> register(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email already exists");
        }

        user.setUserStatus(UserStatus.PENDING);
        User saved = userRepository.save(user);

        return ResponseEntity.ok(toUserResponse(saved));
    }

    @Override
    public ResponseEntity<?> login(String email, String password) {

        User dbUser = userRepository.findByEmail(email);

        if (dbUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        if (!dbUser.getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Incorrect password");
        }

        return ResponseEntity.ok(toUserResponse(dbUser));
    }

    @Override
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Logout successful");
    }

    // The frontend needs id/uniqueId back from register+login to make any
    // subsequent property/ledger call (they all take these as path params).
    // Password is deliberately excluded from the response.
    private Map<String, Object> toUserResponse(User user) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId());
        response.put("uniqueId", user.getUniqueId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("walletAddress", user.getWalletAddress());
        return response;
    }
}
