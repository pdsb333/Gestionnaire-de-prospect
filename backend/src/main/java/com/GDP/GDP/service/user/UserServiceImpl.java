package com.GDP.GDP.service.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.GDP.GDP.dto.user.UserDeleteRequest;
import com.GDP.GDP.dto.user.UserPasswordUpdateRequest;
import com.GDP.GDP.dto.user.UserResponse;
import com.GDP.GDP.dto.user.UserUpdateRequest;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.exception.EmailAlreadyExistsException;
import com.GDP.GDP.exception.InvalidCredentialsException;
import com.GDP.GDP.exception.ResourceNotFoundException;
import com.GDP.GDP.repository.BusinessRepository;
import com.GDP.GDP.repository.UserRepository;
import com.GDP.GDP.service.JwtService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserServiceImpl(
            UserRepository userRepository,
            BusinessRepository businessRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    private void assertCurrentPasswordMatches(User user, String currentPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(User user) {
        return UserResponse.fromEntity(user);
    }

    @Override
    public ProfileUpdateResult updateProfile(User user, UserUpdateRequest request) {
        assertCurrentPasswordMatches(user, request.currentPassword());

        boolean emailChanged = !user.getEmail().equals(request.email());
        if (emailChanged && userRepository.existsByEmailAndIdNot(request.email(), user.getId())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        user.setPseudo(request.pseudo());
        user.setEmail(request.email());

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // Same non-atomic check-then-act race as BusinessServiceImpl.updateBusiness(): the
            // (email) unique constraint is the real guard, this just keeps the loser's error
            // consistent with the non-racy case instead of a generic 409.
            throw new EmailAlreadyExistsException(request.email());
        }

        String newToken = emailChanged ? jwtService.generateToken(user.getEmail()) : null;
        return new ProfileUpdateResult(UserResponse.fromEntity(user), newToken);
    }

    @Override
    public void updatePassword(User user, UserPasswordUpdateRequest request) {
        assertCurrentPasswordMatches(user, request.currentPassword());
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public void deleteAccount(User user, UserDeleteRequest request) {
        assertCurrentPasswordMatches(user, request.currentPassword());

        // `user` is loaded by the JWT filter in its own short-lived transaction, so it's detached
        // by the time it reaches this method. Deleting it directly goes through
        // EntityManager.merge() inside JpaRepository.delete(), which does not reliably cascade to
        // businesses added after `user` was originally loaded (confirmed via CI: `delete from
        // users` was issued with no `delete from businesses` before it, aborting on the FK).
        // Re-fetching by id gives Hibernate a genuinely managed entity in the current persistence
        // context to cascade from, instead of one merged in from a stale detached instance.
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", user.getId()));

        // Same trap as BusinessServiceImpl.deleteBusiness(): prime the persistence context with
        // the full business graph so Hibernate doesn't lazy-load jobOffersList/professionalsList
        // (and each JobOffer's Application) one query at a time while cascading.
        businessRepository.findByUserIdWithJobOffers(user.getId());
        businessRepository.findByUserIdWithProfessionals(user.getId());

        userRepository.delete(managedUser);
    }
}
