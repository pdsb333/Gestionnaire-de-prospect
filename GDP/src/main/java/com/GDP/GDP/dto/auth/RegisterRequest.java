package com.GDP.GDP.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {
    @NotBlank
    private String pseudo;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    public String getPseudo(){ return pseudo; }

    public void setPseudo(String pseudo){ this.pseudo = pseudo;}

    public String getEmail() { return email;}

    public void setEmail(String email) { this.email = email; }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
