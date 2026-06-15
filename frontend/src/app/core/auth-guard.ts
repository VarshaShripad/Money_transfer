import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthToken } from './auth-token';

export const authGuard: CanActivateFn = () => {
  const tokenStore = inject(AuthToken);
  const router = inject(Router);

  if (tokenStore.getToken()) return true;

  router.navigateByUrl('/login');
  return false;
};
