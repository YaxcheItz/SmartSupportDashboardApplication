import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    const isStaffRoute = state.url.includes('/dashboard') || state.url.includes('/reportes');
    
    // Si intenta ir a una ruta de agentes y no es agente, mandarlo a su portal
    if (isStaffRoute && !authService.isStaff()) {
      router.navigate(['/portal']);
      return false;
    }
    
    // Si es agente e intenta ir al portal de cliente, mejor mandarlo al dashboard
    if (state.url.includes('/portal') && authService.isStaff()) {
      router.navigate(['/dashboard']);
      return false;
    }

    return true; 
  } else {
    router.navigate(['/login']);
    return false;
  }
};