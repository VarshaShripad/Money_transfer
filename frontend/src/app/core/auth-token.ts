import { Injectable } from '@angular/core';

type Role = 'ROLE_ADMIN' | 'ROLE_USER' | string;

@Injectable({ providedIn: 'root' })
export class AuthToken {
  private TOKEN_KEY = 'mt_token';
  private ACCOUNT_ID_KEY = 'mt_account_id';
  private USERNAME_KEY = 'mt_username'; // 

  setSession(token: string, accountId: number) {
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.ACCOUNT_ID_KEY, String(accountId));
  }

  setUsername(username: string) {
    localStorage.setItem(this.USERNAME_KEY, username);
  }

  getUsername(): string | null {
    return localStorage.getItem(this.USERNAME_KEY);
  }

  clear() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.ACCOUNT_ID_KEY);
    localStorage.removeItem(this.USERNAME_KEY); 
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getAccountId(): number | null {
    const v = localStorage.getItem(this.ACCOUNT_ID_KEY);
    return v ? Number(v) : null;
  }

  getRole(): Role | null {
    const token = this.getToken();
    if (!token) return null;

    const payload = decodeJwtPayload(token);
    return (payload?.role as Role) ?? null;
  }
}

function decodeJwtPayload(token: string): any | null {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}
