package com.joud.trustchain.user.auth;

import com.joud.trustchain.user.User;
import com.joud.trustchain.user.UserRepository;
import com.joud.trustchain.user.dto.LoginRequest;
import com.joud.trustchain.user.dto.LoginResponse;
import com.joud.trustchain.user.dto.UserResponse;
import org.springframework.stereotype.Service;



@Service
public class AuthService {

    private final UserRepository userRepository;
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    private UserResponse mapToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setFullName(user.getFullName());
        userResponse.setEmail(user.getEmail());
        userResponse.setRole(user.getRole());
        return userResponse;
    }


    public LoginResponse login(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User doesn't exist"));

        if (!loginRequest.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken("12345678");
        loginResponse.setUser(mapToUserResponse(user));

        return loginResponse;
    }




}
