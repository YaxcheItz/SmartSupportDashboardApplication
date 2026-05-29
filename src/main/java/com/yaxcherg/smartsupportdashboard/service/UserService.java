package com.yaxcherg.smartsupportdashboard.service;

import com.yaxcherg.smartsupportdashboard.model.AppUser;
import com.yaxcherg.smartsupportdashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    public Optional<AppUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void updateProfile(AppUser user, Map<String, String> data) {
        if (data.containsKey("email")) {
            user.setEmail(data.get("email"));
        }

        if (data.containsKey("avatarUrl")) {
            user.setAvatarUrl(data.get("avatarUrl"));
        }

        if (data.containsKey("password") && !data.get("password").isBlank()) {
            user.setPassword(encoder.encode(data.get("password")));
        }

        userRepository.save(user);
    }
}
