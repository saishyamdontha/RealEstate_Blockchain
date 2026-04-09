package com.example.RealEstate2.repository;

import com.example.RealEstate2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    User findByEmail(String email);

    Optional<User> findByUniqueId(String uniqueId);
}
