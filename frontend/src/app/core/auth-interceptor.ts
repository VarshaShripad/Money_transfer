import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthToken } from './auth-token';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStore = inject(AuthToken);
  const token = tokenStore.getToken();

  // without token for login
  if (!token || req.url.includes('/api/v1/auth/login')) {
    return next(req);
  }

  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
