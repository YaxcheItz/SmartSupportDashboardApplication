import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router'; // <- Importa esto
import { Toast } from './components/toast/toast';
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Toast], // <- Y colócalos aquí. (OJO: Ya no importamos TicketList aquí directamente)
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly title = signal('smart-support-dashboard-ui');
}
