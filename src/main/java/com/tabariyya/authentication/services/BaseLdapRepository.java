package com.tabariyya.authentication.services;

import com.waleed.springutils.auth.models.BaseUser;

public interface BaseLdapRepository<T extends BaseUser> {
    T authenticate(String userName, String password);
}
