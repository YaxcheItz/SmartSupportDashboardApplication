import { Component, signal } from '@angular/core';
import { TicketList } from "./components/ticket-list/ticket-list";

@Component({
  selector: 'app-root',
  imports: [TicketList],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('smart-support-dashboard-ui');
}
