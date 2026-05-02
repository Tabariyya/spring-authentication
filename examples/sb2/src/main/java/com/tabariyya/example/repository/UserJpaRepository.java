package com.tabariyya.example.repository;

import com.tabariyya.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserName(String userName);
}
