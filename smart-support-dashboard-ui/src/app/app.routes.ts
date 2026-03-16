import { Routes } from '@angular/router';
import { TicketList } from './components/ticket-list/ticket-list';
import { TicketForm } from './components/ticket-form/ticket-form';
import { TicketDetailComponent } from './components/ticket-detail/ticket-detail';
import { LoginComponent } from './components/login/login'; // <-- IMPORTA EL LOGIN
import { authGuard } from './guards/auth-guard'; // <-- IMPORTA EL GUARDIA

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' }, // <-- Ahora por defecto manda al Login
  { path: 'login', component: LoginComponent },

  // ¡AQUÍ ESTÁ LA MAGIA! Le pusimos canActivate a dashboard y ticket detail
  { path: 'dashboard', component: TicketList, canActivate: [authGuard] },
  { path: 'ticket/:id', component: TicketDetailComponent, canActivate: [authGuard] },

  // El formulario de creación sigue siendo público
  { path: 'nuevo-ticket', component: TicketForm },
];