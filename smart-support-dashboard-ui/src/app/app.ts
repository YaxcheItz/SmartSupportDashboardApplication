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
      style="background: var(--card-bg); padding: 15px 30px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      display: flex; justify-content: space-between; align-items: center;
      border-bottom: 1px solid var(--border-color);"
    >
      <div style="display: flex; gap: 20px; align-items: center;">
        <h1 style="margin: 0; color: var(--primary-color); font-size: 1.5rem;">🤖 Smart Support</h1>

        <!-- Navegación -->
        <nav style="display: flex; gap: 15px;">
          <a
            routerLink="/dashboard"
            style="text-decoration: none; color: var(--text-color); font-weight: bold;"
          >
            Dashboard
          </a>

          <a
            routerLink="/nuevo-ticket"
            style="text-decoration: none; color: var(--text-color); font-weight: bold;"
          >
            Crear Ticket
          </a>
        </nav>
      </div>

      <!-- BOTÓN MODO OSCURO -->
      <button
        (click)="themeService.toggleTheme()"
        style="background: transparent; border: 2px solid var(--border-color);
        color: var(--text-color); padding: 8px 15px; border-radius: 20px;
        cursor: pointer; font-weight: bold; display: flex; align-items: center; gap: 8px;"
      >
        @if (themeService.isDarkMode()) {
          <span>☀️ Modo Claro</span>
        } @else {
          <span>🌙 Modo Oscuro</span>
        }
      </button>
    </header>

    <!-- CONTENIDO PRINCIPAL -->
    <main style="padding: 20px;">
      <router-outlet></router-outlet>
    </main>

    <!-- TOAST GLOBAL -->
    <app-toast></app-toast>
  `,
})
export class App {
  // Inyectamos el servicio
  themeService = inject(ThemeService);
}
