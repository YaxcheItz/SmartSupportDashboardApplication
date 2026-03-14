import { Routes } from '@angular/router';
import { TicketList } from './components/ticket-list/ticket-list';
import { TicketForm } from './components/ticket-form/ticket-form';
import { TicketDetailComponent } from './components/ticket-detail/ticket-detail';
// Las rutas de nuestra aplicación
export const routes: Routes = [
  // Ruta por defecto: Cuando entras a localhost:4200, te manda al dashboard
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

  // Ruta para ver todos los tickets (Vista del Agente)
  { path: 'dashboard', component: TicketList },

  // ¡Ojo! Dejaremos esta ruta lista para el componente que crearemos en un momento
  { path: 'nuevo-ticket', component: TicketForm },

  { path: 'ticket/:id', component: TicketDetailComponent },
];
