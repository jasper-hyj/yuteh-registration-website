package com.yuteh.register.user;

import com.yuteh.register.user.model.User;

import java.util.List;

public interface UserService {
    /**
     * Get user method
     *
     * @param user current user
     */
    User getUser(User user);

    List<User> getUserList();
}
