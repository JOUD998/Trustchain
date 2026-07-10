package com.joud.trustchain.auth;

import com.joud.trustchain.security.JwtService;
import com.joud.trustchain.user.Role;
import com.joud.trustchain.user.User;
import com.joud.trustchain.user.UserRepository;
import com.joud.trustchain.auth.dto.LoginRequest;
import com.joud.trustchain.auth.dto.LoginResponse;
import com.joud.trustchain.auth.dto.RegisterRequest;
import com.joud.trustchain.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
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

        if (!passwordEncoder.matches(loginRequest.getPassword(),user.getPassword()))
            throw new RuntimeException("Wrong password");

        LoginResponse loginResponse = new LoginResponse();

        loginResponse.setToken(jwtService.generateToken(user));
        loginResponse.setUser(mapToUserResponse(user));

        return loginResponse;
    }

    public LoginResponse register(RegisterRequest registerRequest) {


        Optional<User> userCheck = userRepository.findByEmail(registerRequest.getEmail());
        LoginResponse loginResponse = new LoginResponse();

        if (userCheck.isPresent())
            throw new RuntimeException("Email is already taken");



        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.ADMIN)
                .build();
        user = userRepository.save(user);

        loginResponse.setUser(mapToUserResponse(user));
        loginResponse.setToken(jwtService.generateToken(user));
        return loginResponse;


    }










}
