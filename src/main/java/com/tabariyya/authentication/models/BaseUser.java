package com.tabariyya.authentication.models;


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

}
