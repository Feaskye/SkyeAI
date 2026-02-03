package com.skyeai.jarvis.user.repository;

import com.skyeai.jarvis.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.enabled = true")
    Iterable<User> findAllEnabled();

    @Query("SELECT u FROM User u WHERE u.verified = true")
    Iterable<User> findAllVerified();

    @Query("SELECT u FROM User u WHERE u.role = ?1")
    Iterable<User> findAllByRole(String role);
}
