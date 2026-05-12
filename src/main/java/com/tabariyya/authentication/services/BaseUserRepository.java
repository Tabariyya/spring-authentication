package com.tabariyya.authentication.services;

import com.tabariyya.authentication.models.BaseUser;

import java.util.Optional;

public interface BaseUserRepository<T extends BaseUser> {

    T save(T user);
    Optional<T> findByUserName(String userName);
    Optional<T> findById(Object id);

}
