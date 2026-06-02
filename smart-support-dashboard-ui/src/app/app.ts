import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet, RouterModule } from '@angular/router';
import { Toast } from './components/toast/toast';
import { ThemeService } from './services/theme';
import { AuthService } from './services/auth';
import { WebSocketService } from './services/websocket';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterModule, Toast, CommonModule],
  template: `
    <!-- HEADER GLOBAL SSD -->
    <header class="global-header">
      
      <!-- Lado Izquierdo: Navegación -->
      <div class="header-section left">
        @if (authService.isLoggedIn()) {
          <nav class="global-nav">
            @if (authService.isStaff()) {
              <a routerLink="/dashboard" routerLinkActive="active-link" class="nav-link">Dashboard</a>
              @if (authService.isAdmin()) {
                <a routerLink="/reportes" routerLinkActive="active-link" class="nav-link">Reportes</a>
              }
            } @else {
              <a routerLink="/portal" routerLinkActive="active-link" class="nav-link">Mi Portal</a>
            }
            <a routerLink="/nuevo-ticket" routerLinkActive="active-link" class="nav-link">Tickets</a>
          </nav>
        }
      </div>

      <!-- Centro: Logo SSD -->
      <div class="header-section center">
        <h1 class="logo" [routerLink]="authService.isStaff() ? '/dashboard' : '/portal'">
          Smart Support Dashboard
        </h1>
      </div>

      <!-- Lado Derecho: Usuario y Config -->
      <div class="header-section right">
        @if (authService.isLoggedIn()) {
          <div class="user-profile-nav" [routerLink]="['/profile']">
            <div class="avatar-circle">
              <img *ngIf="authService.currentUser()?.avatarUrl" [src]="authService.currentUser()?.avatarUrl" alt="Avatar">
              <span *ngIf="!authService.currentUser()?.avatarUrl">{{ authService.currentUser()?.username?.charAt(0)?.toUpperCase() }}</span>
            </div>
            <span class="username-text">{{ authService.currentUser()?.username }}</span>
          </div>
          
          <div class="divider"></div>
        }

        <button (click)="themeService.toggleTheme()" class="theme-btn" [title]="themeService.isDarkMode() ? 'Modo Claro' : 'Modo Oscuro'">
          {{ themeService.isDarkMode() ? '☀️' : '🌙' }}
        </button>

        @if (authService.isLoggedIn()) {
          <button (click)="logout()" class="logout-btn" title="Cerrar Sesión">
            <span class="icon">Logout</span>
          </button>
        }
      </div>
    </header>

    <!-- CONTENIDO PRINCIPAL -->
    <main class="main-container">
      <router-outlet></router-outlet>
    </main>

    <app-toast></app-toast>
  `,
  styles: [`
    .global-header {
      display: grid;
      grid-template-columns: 1fr auto 1fr;
      align-items: center;
      padding: 0 2rem;
      height: 70px;
      background: var(--card-bg);
      border-bottom: 1px solid var(--border-color);
      position: sticky;
      top: 0;
      z-index: 1000;
    }

    .header-section { display: flex; align-items: center; }
    .header-section.center { justify-content: center; }
    .header-section.right { justify-content: flex-end; gap: 1rem; }

    .logo {
      font-size: 1.25rem;
      font-weight: 800;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      margin: 0;
      cursor: pointer;
      color: var(--text-color);
      white-space: nowrap;
    }

    .global-nav {
      display: flex;
      gap: 2rem;
    }

    .nav-link {
      color: var(--text-muted);
      font-weight: 600;
      font-size: 0.8rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      transition: all 0.2s;
      text-decoration: none;
    }

    .nav-link:hover, .nav-link.active-link {
      color: var(--text-color);
    }

    .user-profile-nav {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      cursor: pointer;
      padding: 0.5rem;
      border-radius: var(--radius-md);
      transition: background 0.2s;
    }

    .user-profile-nav:hover {
      background: var(--bg-color);
    }

    .avatar-circle {
      width: 32px;
      height: 32px;
      background: var(--primary-color);
      color: var(--bg-color);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 0.8rem;
      overflow: hidden;
    }

    .avatar-circle img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .username-text {
      font-size: 0.9rem;
      font-weight: 600;
      color: var(--text-color);
    }

    .divider {
      width: 1px;
      height: 24px;
      background: var(--border-color);
    }

    .theme-btn, .logout-btn {
      background: none;
      border: 1px solid var(--border-color);
      width: 36px;
      height: 36px;
      border-radius: 50%;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;
      font-size: 1.1rem;
    }

    .theme-btn:hover, .logout-btn:hover {
      background: var(--bg-color);
      border-color: var(--text-muted);
    }

    .logout-btn {
      color: var(--danger-color);
      border-color: transparent;
      font-size: 0.7rem;
      font-weight: 700;
      text-transform: uppercase;
    }

    @media (max-width: 1000px) {
      .global-header {
        grid-template-columns: 1fr 1fr;
        height: auto;
        padding: 1rem;
        gap: 1rem;
      }
      .header-section.center { order: -1; grid-column: span 2; }
      .header-section.left { justify-content: flex-start; }
      .logo { font-size: 1.1rem; }
    }

    @media (max-width: 600px) {
      .global-nav { gap: 1rem; }
      .username-text { display: none; }
    }
  `]
})
export class App implements OnInit {
  themeService = inject(ThemeService);
  authService = inject(AuthService);
  wsService = inject(WebSocketService);

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.wsService.connect();
    }
  }

  logout() {
    this.wsService.disconnect();
    this.authService.logout();
    window.location.href = '/login';
  }
}
