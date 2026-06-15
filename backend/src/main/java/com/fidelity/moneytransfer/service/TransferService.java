package com.fidelity.moneytransfer.service;

import com.fidelity.moneytransfer.dto.TransferRequest;
import com.fidelity.moneytransfer.dto.TransferResponse;

public interface TransferService {

    TransferResponse transfer(TransferRequest request);
}
