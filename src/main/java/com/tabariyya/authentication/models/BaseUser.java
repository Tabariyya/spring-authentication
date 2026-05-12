package com.tabariyya.authentication.models;

import java.time.LocalDateTime;

public interface BaseUser {

    Object getId();
    void setId(Object id);

    // String userName
    String getUserName();
    void setUserName(String userName);

    // String password
    String getPassword();
    void setPassword(String password);

    // String email
    String getEmail();
    void setEmail(String email);

    // LocalDateTime createdAt
    LocalDateTime getCreatedAt();
    void setCreatedAt(LocalDateTime createdAt);

}
