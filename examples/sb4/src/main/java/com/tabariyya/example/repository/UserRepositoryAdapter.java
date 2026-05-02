package com.tabariyya.example.repository;

import com.tabariyya.authentication.services.BaseUserRepository;
import com.tabariyya.example.entity.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter implements BaseUserRepository<User> {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public Optional<User> findByUserName(String userName) {
        return jpaRepository.findByUserName(userName);
    }

    @Override
    public Optional<User> findById(int id) {
        return jpaRepository.findById(id);
    }
}
