import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthToken } from '../core/auth-token';
import { map, tap } from 'rxjs/operators';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  role: 'ROLE_ADMIN' | 'ROLE_USER';
  accountId: number;
}

export interface SignupRequest {
  username: string;
  password: string;
  holderName: string;
}

export interface SignupResponse {
  message: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient, private tokenStore: AuthToken) {}

  login(body: LoginRequest) {
    return this.http
      .post<{ token: string; accountId: number }>('/api/v1/auth/login', body)
      .pipe(
        tap((res) => {
          this.tokenStore.setSession(res.token, res.accountId);
          this.tokenStore.setUsername(body.username); 
        })
      );
  }

  signup(body: SignupRequest) {
    return this.http.post('/api/v1/auth/signup', body, { responseType: 'text' });
  }

  logout() {
    this.tokenStore.clear();
  }

  get accountId(): number | null {
    return this.tokenStore.getAccountId();
  }

  get username(): string | null {
    return this.tokenStore.getUsername();
  }

  get role(): string | null {
    return this.tokenStore.getRole();
  }

  get isAdmin(): boolean {
    return this.role === 'ROLE_ADMIN';
  }

}
