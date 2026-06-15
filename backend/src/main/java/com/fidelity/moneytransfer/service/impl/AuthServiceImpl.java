package com.fidelity.moneytransfer.service.impl;

import java.math.BigDecimal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fidelity.moneytransfer.domain.Account;
import com.fidelity.moneytransfer.domain.User;
import com.fidelity.moneytransfer.dto.AuthRequest;
import com.fidelity.moneytransfer.dto.AuthResponse;
import com.fidelity.moneytransfer.dto.SignupRequest;
import com.fidelity.moneytransfer.enums.AccountStatus;
import com.fidelity.moneytransfer.exception.UsernameAlreadyExistsException;
import com.fidelity.moneytransfer.repository.AccountRepository;
import com.fidelity.moneytransfer.repository.UserRepository;
import com.fidelity.moneytransfer.security.JwtUtil;
import com.fidelity.moneytransfer.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           AccountRepository accountRepository,
                           JwtUtil jwtUtil,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse login(AuthRequest request) {

        // 1️⃣ Validate user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // 2️⃣ Generate JWT
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole()
        );

        // 3️⃣ Fetch account for this user
        Account account = accountRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Account not found for user"));

        // 4️⃣ Return token + accountId
        return new AuthResponse(token, account.getId());
    }

    @Override
    @Transactional
    public void signup(SignupRequest request) {

        if (userRepository.existsById(request.getUsername())) {
            throw new UsernameAlreadyExistsException(request.getUsername());
        }

        // Create User
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");

        userRepository.save(user);

        // Create Account
        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setHolderName(request.getHolderName());
        account.setBalance(BigDecimal.valueOf(1000));
        account.setStatus(AccountStatus.ACTIVE);

        accountRepository.save(account);
    }
}
