import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export type AccountStatus = 'ACTIVE' | 'CLOSED' | 'LOCKED';

export interface AccountResponse {
  id: number;
  holderName: string;
  balance: number;
  status: AccountStatus;
  lastUpdated: string;
}

export type TxStatus = 'SUCCESS' | 'FAILED';

export interface TransactionResponse {
  id: string;
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  status: TxStatus;
  failureReason: string | null;
  idempotencyKey: string;
  createdOn: string;
}

@Injectable({ providedIn: 'root' })
export class AccountService {
  constructor(private http: HttpClient) {}

  getAccount(id: number) {
    return this.http.get<AccountResponse>(`/api/v1/accounts/${id}`);
  }

  getBalance(id: number) {
    return this.http.get<number>(`/api/v1/accounts/${id}/balance`);
  }

  getTransactions(id: number) {
    return this.http.get<TransactionResponse[]>(`/api/v1/accounts/${id}/transactions`);
  }
}
