package com.quantumleap.repository;

import com.quantumleap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email
     *
     * @param email user email
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email
     *
     * @param email user email
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find user by email (case-insensitive)
     *
     * @param email user email
     * @return Optional containing user if found
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    /**
     * Find users by name containing (case-insensitive)
     *
     * @param name name to search for
     * @return list of users with matching names
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    java.util.List<User> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find user by exact name
     *
     * @param name user name
     * @return Optional containing user if found
     */
    Optional<User> findByName(String name);
}
