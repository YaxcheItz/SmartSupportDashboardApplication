import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterModule } from '@angular/router';
import { Toast } from './components/toast/toast';
import { ThemeService } from './services/theme';
import { AuthService } from './services/auth';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterModule, Toast],
  template: `
    <!-- HEADER GLOBAL -->
    <header class="global-header">
      <div class="header-left">
        <h1 class="logo" routerLink="/dashboard">
          <span class="logo-icon">S</span>
          <span style="color: var(--text-color);">Smart</span><span style="color: var(--primary-color);">Support</span>
        </h1>

        <!-- Navegación (Solo visible si está logueado) -->
        @if (authService.isLoggedIn()) {
          <nav class="global-nav">
            <a routerLink="/dashboard" routerLinkActive="active-link" class="nav-link">
              Dashboard
            </a>
            <a routerLink="/nuevo-ticket" routerLinkActive="active-link" class="nav-link">
              Crear Ticket
            </a>
          </nav>
        }
      </div>

      <!-- BOTONES DERECHOS -->
      <div class="header-right" style="display: flex; gap: 1rem; align-items: center;">
        
        @if (authService.isLoggedIn()) {
          <div class="user-info" style="display: flex; align-items: center; gap: 0.5rem;">
            <span style="font-weight: 500; font-size: 0.9rem; color: var(--text-muted);">{{ authService.getCurrentUser()?.username }}</span>
            <button (click)="logout()" class="btn btn-outline" style="padding: 0.4rem 0.8rem; font-size: 0.8rem; border-color: var(--danger-color); color: var(--danger-color);">Salir</button>
          </div>
        }

        <!-- BOTÓN MODO OSCURO -->
        <button (click)="themeService.toggleTheme()" class="btn btn-outline" style="border-radius: var(--radius-full); padding: 0.5rem 1rem; font-size: 0.875rem;">
          @if (themeService.isDarkMode()) {
            <span style="display:flex; align-items:center; gap:6px;">☀️ <span style="font-weight: 500;">Claro</span></span>
          } @else {
            <span style="display:flex; align-items:center; gap:6px;">🌙 <span style="font-weight: 500;">Oscuro</span></span>
          }
        </button>
      </div>
    </header>

    <!-- CONTENIDO PRINCIPAL -->
    <main class="main-container">
      <router-outlet></router-outlet>
    </main>

    <!-- TOAST GLOBAL -->
    <app-toast></app-toast>
  `,
})
export class App {
  themeService = inject(ThemeService);
  authService = inject(AuthService);

  logout() {
    this.authService.logout();
    window.location.href = '/login';
  }
}
