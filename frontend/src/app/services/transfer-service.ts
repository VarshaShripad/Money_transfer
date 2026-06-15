import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface TransferRequest {
  fromAccountId: number;
  toAccountId: number;
  amount: number;
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
  constructor(private http: HttpClient) {}

  transfer(body: TransferRequest) {
    return this.http.post<TransferResponse>('/api/v1/transfers', body);
  }
}
