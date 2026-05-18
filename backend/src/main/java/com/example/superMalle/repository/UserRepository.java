package com.example.superMalle.repository;

import com.example.superMalle.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE :term OR LOWER(u.email) LIKE :term OR LOWER(u.phone) LIKE :term")
    Page<User> searchUsers(@Param("term") String term, Pageable pageable);
}
