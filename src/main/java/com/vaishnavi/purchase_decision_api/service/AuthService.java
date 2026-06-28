package com.vaishnavi.purchase_decision_api.service;

import com.vaishnavi.purchase_decision_api.dtos.AuthResponse;
import com.vaishnavi.purchase_decision_api.dtos.LoginRequest;
import com.vaishnavi.purchase_decision_api.dtos.RegisterRequest;
import com.vaishnavi.purchase_decision_api.entity.User;
import com.vaishnavi.purchase_decision_api.exceptions.EmailAlreadyExistsException;
import com.vaishnavi.purchase_decision_api.exceptions.InvalidPasswordException;
import com.vaishnavi.purchase_decision_api.exceptions.UserNotFoundException;
import com.vaishnavi.purchase_decision_api.repository.UserRepository;
import com.vaishnavi.purchase_decision_api.security.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(user.getName(), user.getEmail(), token);
    }

    public AuthResponse login(LoginRequest loginRequest){

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()-> new UserNotFoundException("No account found with this email"));

        if(!passwordEncoder.matches(loginRequest.getPassword(),user.getPassword())){
            throw new InvalidPasswordException("Incorrect password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(user.getName(), user.getEmail(), token);



    }
}