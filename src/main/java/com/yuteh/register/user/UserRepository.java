package com.yuteh.register.user;

import com.yuteh.register.user.model.User;

import java.util.List;

public interface UserRepository {
    /**
     * Get user information using email
     * @param email email of the user (email is the key)
     * @return user information from the db (return null if user not exist)
     */
    User select(String email);

    List<User> selectList();
}
