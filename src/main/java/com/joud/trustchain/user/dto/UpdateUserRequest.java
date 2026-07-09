package com.joud.trustchain.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class UpdateUserRequest {

    private String fullName;
    @Email
    private String email;

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
