import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { RedirectService } from '../services/redirect.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const redirectService = inject(RedirectService);

  if (authService.isAuthenticated()) {
    return true;
  }

  redirectService.set(state.url);
  return router.createUrlTree(['/login']);
};
