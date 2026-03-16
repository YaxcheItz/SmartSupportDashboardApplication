import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterModule } from '@angular/router';
import { Toast } from './components/toast/toast';
import { ThemeService } from './services/theme';

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

        <!-- Navegación -->
        <nav class="global-nav">
          <a routerLink="/dashboard" routerLinkActive="active-link" class="nav-link">
            Dashboard
          </a>
          <a routerLink="/nuevo-ticket" routerLinkActive="active-link" class="nav-link">
            Crear Ticket
          </a>
        </nav>
      </div>

      <!-- BOTÓN MODO OSCURO -->
      <div class="theme-toggle-container">
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
}
