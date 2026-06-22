import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CryptoService } from './crypto-service';

export interface TransferRequest {
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  idempotencyKey: string;
}

export interface EncryptedTransferRequest {
  encryptedToAccountId: string;
  encryptedAmount: string;
  idempotencyKey: string;
}

export interface TransferResponse {
  transactionId: string;
  status: 'SUCCESS' | 'FAILED';
  message: string;
  debitedFrom: number;
  creditedTo: number;
  amount: number;
}

@Injectable({ providedIn: 'root' })
export class TransferService {
  constructor(
    private http: HttpClient,
    private crypto: CryptoService
  ) {}

  /**
   * Standard transfer (deprecated in favor of transferSecure)
   */
  transfer(body: TransferRequest) {
    return this.http.post<TransferResponse>('/api/v1/transfers', body);
  }

  /**
   * Secure transfer with encrypted toAccountId and amount
   * Prevents network monitors from seeing the actual recipient and amount
   */
  transferSecure(body: TransferRequest) {
    // Encrypt sensitive fields
    const encryptedRequest: EncryptedTransferRequest = {
      encryptedToAccountId: this.crypto.encryptLong(body.toAccountId),
      encryptedAmount: this.crypto.encrypt(body.amount.toString()),
      idempotencyKey: body.idempotencyKey
    };

    return this.http.post<TransferResponse>('/api/v1/transfers/encrypted', encryptedRequest);
  }
}
