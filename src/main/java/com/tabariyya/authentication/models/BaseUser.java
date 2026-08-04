package com.tabariyya.authentication.models;


public interface BaseUser<ID> {

    ID getId();
    void setId(ID id);

    // String identifier
    String getUserName();
    void setUserName(String userName);

    // String password
    String getPassword();
    void setPassword(String password);

    String getContactInfo();
    void setContactInfo(String contactInfo);

    String getContactInfoType();
    void setContactInfoType(String contactInfoType);

    boolean isDeleted();

}
