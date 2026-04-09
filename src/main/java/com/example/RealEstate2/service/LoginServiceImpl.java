package com.example.RealEstate2.service;


import com.example.RealEstate2.model.User;
import com.example.RealEstate2.model.status.UserStatus;
import com.example.RealEstate2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
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

        return ResponseEntity.ok("Login successful");
    }

    @Override
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Logout successful");
    }
}