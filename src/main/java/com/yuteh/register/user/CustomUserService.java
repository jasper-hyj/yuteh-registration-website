package com.yuteh.register.user;

import com.yuteh.register.user.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserService implements UserService {
    private final UserRepository userRepository;

    public CustomUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUser(User user) {
        return userRepository.select(user.getEmail());
    }

    @Override
    public List<User> getUserList() {
        return userRepository.selectList();
    }
}
