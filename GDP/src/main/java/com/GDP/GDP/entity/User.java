package com.GDP.GDP.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import jakarta.persistence.*;


@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition="BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    private String pseudo;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Business> businesses = new ArrayList<>();

    public User() {
    }

    public User(String pseudo, String email, String password, Role role) {
        this.pseudo = pseudo;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public enum Role{
        ROLE_USER,
        ROLE_ADMIN
    }
    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Business> getBusinesses() {
        return businesses;
    }
    
    public void setBusinesses(List<Business> businesses) {
        this.businesses = businesses;
    }
    
}
