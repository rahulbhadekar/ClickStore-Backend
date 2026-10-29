
package com.clickstore.clickstore.service;

import com.clickstore.clickstore.dto.LoginRequest;
import com.clickstore.clickstore.dto.LoginResponse;
import com.clickstore.clickstore.dto.RegisterRequest;
import com.clickstore.clickstore.entity.User;
import com.clickstore.clickstore.entity.UserContact;
import com.clickstore.clickstore.repository.UserContactRepository;
import com.clickstore.clickstore.repository.UserRepository;
import com.clickstore.clickstore.repository.UserRoleRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserContactRepository contactRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            UserContactRepository contactRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {

        String fullName = request.getFullName().trim();
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (contactRepository
                .findByContactTypeAndContactValue("EMAIL", email)
                .isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        User user = new User();
        user.setFullName(fullName);
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        user.setAccountStatus("ACTIVE");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);

        UserContact contact = new UserContact();
        contact.setUserId(savedUser.getId());
        contact.setContactType("EMAIL");
        contact.setContactValue(email);
        contact.setVerified(false);
        contact.setPrimary(true);
        contact.setCreatedAt(now);
        contact.setUpdatedAt(now);

        contactRepository.save(contact);

        // Assign CUSTOMER role to newly registered users.
        userRoleRepository.assignCustomerRole(savedUser.getId());

        String token = jwtService.generateToken(
                savedUser.getId(),
                email
        );

        List<String> roles =
                userRoleRepository.findRoleNamesByUserId(savedUser.getId());

        return new LoginResponse(
                "Registration successful",
                token,
                savedUser.getId(),
                savedUser.getFullName(),
                email,
                roles
        );
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        var contact = contactRepository
                .findByContactTypeAndContactValue("EMAIL", email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        User user = userRepository.findById(contact.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (!"ACTIVE".equals(user.getAccountStatus())
                || !passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        user.setLastLoginAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        userRepository.save(user);

        String token = jwtService.generateToken(
                user.getId(),
                email
        );

        List<String> roles =
                userRoleRepository.findRoleNamesByUserId(user.getId());

        return new LoginResponse(
                "Login successful",
                token,
                user.getId(),
                user.getFullName(),
                contact.getContactValue(),
                roles
        );
    }
}
