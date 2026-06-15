package com.joud.trustchain.user.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.joud.trustchain.user.Role;

@JsonPropertyOrder({

        "id",

        "fullName",

        "email",

        "role"

})
public class UserResponse {

    private String fullName;


    private String email;


    private Long id;

    private Role role;


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
