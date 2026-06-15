package com.joud.trustchain.user;

import com.joud.trustchain.user.dto.CreateUserRequest;
import com.joud.trustchain.user.dto.UpdateUserRequest;
import com.joud.trustchain.user.dto.UserResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
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


    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userResponses = new ArrayList<>();

        for (User user : users) {
            UserResponse userResponse = mapToUserResponse(user);
            userResponses.add(userResponse);
        }

        return userResponses;

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


    public UserResponse createUser(CreateUserRequest request) {

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(Role.CUSTOMER)
                .build();
        user =  userRepository.save(user);
        UserResponse userResponse = mapToUserResponse(user);
        return userResponse;
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

//        user = userRepository.save(user);
//        UserResponse userResponse = mapToUserResponse(user);
//        return userResponse;

        return mapToUserResponse(userRepository.save(user));
    }


}