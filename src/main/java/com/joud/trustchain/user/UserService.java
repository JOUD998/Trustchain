package com.joud.trustchain.user;

import com.joud.trustchain.security.JwtService;
import com.joud.trustchain.user.dto.CreateUserRequest;
import com.joud.trustchain.user.dto.UpdateUserRequest;
import com.joud.trustchain.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }




    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userResponses = new ArrayList<>();

        for (User user : users) {
            UserResponse userResponse = mapToUserResponse(user);
            userResponses.add(userResponse);
        }

        return userResponses;

    }


    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already taken");
        }

        if (request.getRole() == Role.ADMIN) {
            throw new RuntimeException(
                    "Creating another administrator through this endpoint is not allowed"
            );
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    public User findUserEntityById(Long id) {

        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));

    }

    public UserResponse findUserById(Long id) {
        User user = findUserEntityById(id);
        return mapToUserResponse(user);

    }

    public void deleteUserById(Long id) {
        findUserEntityById(id);
        userRepository.deleteById(id);
    }




    public UserResponse updateUser(Long id, UpdateUserRequest request) {

        User user = findUserEntityById(id);

        if (request.getFullName() != null) {
            if (request.getFullName().isBlank()) {
                throw new RuntimeException("Full name cannot be blank");
            }
            user.setFullName(request.getFullName());


        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }


        return mapToUserResponse(userRepository.save(user));
    }


    public UserResponse getCurrentUser(String token) {

        Long userId = jwtService.extractUserId(token);
        return findUserById(userId);

    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setFullName(user.getFullName());
        userResponse.setEmail(user.getEmail());
        userResponse.setRole(user.getRole());
        return userResponse;
    }


}