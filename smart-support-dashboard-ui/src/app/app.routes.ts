import { Routes } from '@angular/router';
import { TicketList } from './components/ticket-list/ticket-list';
import { TicketForm } from './components/ticket-form/ticket-form';
import { TicketDetailComponent } from './components/ticket-detail/ticket-detail';
import { LoginComponent } from './components/login/login';
import { Reports } from './components/reports/reports';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: TicketList, canActivate: [authGuard] },
  { path: 'ticket/:id', component: TicketDetailComponent, canActivate: [authGuard] },
  { path: 'reportes', component: Reports, canActivate: [authGuard] },
  { path: 'nuevo-ticket', component: TicketForm },
];