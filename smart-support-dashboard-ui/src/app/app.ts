import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterModule } from '@angular/router';
import { Toast } from './components/toast/toast';
import { ThemeService } from './services/theme';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterModule, Toast],
  template: `
    <!-- HEADER GLOBAL -->
    <header
      style="background-color: var(--card-bg); padding: 1rem 2rem; box-shadow: var(--shadow-sm);
      display: flex; justify-content: space-between; align-items: center;
      position: sticky; top: 0; z-index: 50; border-bottom: 1px solid var(--border-color);"
    >
      <div style="display: flex; gap: 2.5rem; align-items: center;">
        <h1 style="margin: 0; color: var(--primary-color); font-size: 1.25rem; font-weight: 700; display: flex; align-items: center; gap: 8px; cursor: pointer;" routerLink="/dashboard">
          <span style="font-size: 1.5rem; background: var(--primary-color); color: white; border-radius: 8px; width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;">S</span>
          <span style="color: var(--text-color);">Smart</span><span style="color: var(--primary-color);">Support</span>
        </h1>

        <!-- Navegación -->
        <nav style="display: flex; gap: 1.5rem;">
          <a
            routerLink="/dashboard"
            routerLinkActive="active-link"
            style="text-decoration: none; color: var(--text-muted); font-weight: 500; font-size: 0.95rem; transition: color 0.2s;"
            onmouseover="this.style.color='var(--text-color)'"
            onmouseout="this.style.color='var(--text-muted)'"
          >
            Dashboard
          </a>

          <a
            routerLink="/nuevo-ticket"
            routerLinkActive="active-link"
            style="text-decoration: none; color: var(--text-muted); font-weight: 500; font-size: 0.95rem; transition: color 0.2s;"
            onmouseover="this.style.color='var(--text-color)'"
            onmouseout="this.style.color='var(--text-muted)'"
          >
            Crear Ticket
          </a>
        </nav>
      </div>

      <!-- BOTÓN MODO OSCURO -->
      <button
        (click)="themeService.toggleTheme()"
        class="btn btn-outline"
        style="border-radius: var(--radius-full); padding: 0.5rem 1rem; font-size: 0.875rem;"
      >
        @if (themeService.isDarkMode()) {
          <span style="display:flex; align-items:center; gap:6px;">☀️ <span style="font-weight: 500;">Claro</span></span>
        } @else {
          <span style="display:flex; align-items:center; gap:6px;">🌙 <span style="font-weight: 500;">Oscuro</span></span>
        }
      </button>
    </header>

    <!-- CONTENIDO PRINCIPAL -->
    <main style="padding: 2rem; max-width: 1280px; margin: 0 auto; width: 100%; box-sizing: border-box;">
      <router-outlet></router-outlet>
    </main>

    <!-- TOAST GLOBAL -->
    <app-toast></app-toast>
  `,
})
export class App {
  themeService = inject(ThemeService);
}
