import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketService } from '../../services/ticket';
import { Ticket } from '../../models/ticket.model';
//en esto se le dice al componente que, en cuanto nazca (cuando cargue la página), llame a la API para pedir los tickets.

@Component({
  selector: 'app-ticket-list',
  imports: [CommonModule],
  templateUrl: './ticket-list.html',
  styleUrl: './ticket-list.css',
})
export class TicketList implements OnInit {

  // 1. Usamos 'signal' en lugar de un array normal.
  // Un Signal es como un altavoz que le grita a la pantalla "¡Hey, tengo datos nuevos, dibújate de nuevo!"
  tickets = signal<Ticket[]>([]);

  private ticketService = inject(TicketService);

  ngOnInit(): void {
    this.loadTickets();
  }

  loadTickets(): void {
    this.ticketService.getAllTickets().subscribe({
      next: (data) => {
        // 2. Así se meten los datos en un Signal
        this.tickets.set(data);
        console.log('Tickets cargados:', data);
      },
      error: (error) => {
        console.error('Error al cargar tickets:', error);
      }
    });
  }
}