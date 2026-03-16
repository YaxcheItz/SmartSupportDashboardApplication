import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Le preguntamos al servicio si hay un usuario logueado
  if (authService.isLoggedIn()) {
    return true; // Déjalo pasar
  } else {
    // Si no está logueado, lo pateamos a la pantalla de login
    router.navigate(['/login']);
    return false; // No lo dejes pasar
  }
};