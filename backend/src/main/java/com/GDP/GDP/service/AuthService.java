package com.GDP.GDP.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.GDP.GDP.entity.User;
import com.GDP.GDP.exception.InvalidCredentialsException;
import com.GDP.GDP.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authManager = authManager;
    }


    public String register(String pseudo, String email, String password) {
        User user = new User(
                pseudo,
                email,
                passwordEncoder.encode(password),
                User.Role.ROLE_USER
        );

        userRepository.save(user);

        return jwtService.generateToken(user.getEmail());
    }


    public String login(String email, String password) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        return jwtService.generateToken(user.getEmail());
    }
}
