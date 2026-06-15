package com.fidelity.moneytransfer.service;

import com.fidelity.moneytransfer.dto.AuthRequest;
import com.fidelity.moneytransfer.dto.AuthResponse;
import com.fidelity.moneytransfer.dto.SignupRequest;

public interface AuthService {

    AuthResponse login(AuthRequest request);

    void signup(SignupRequest request);
}
