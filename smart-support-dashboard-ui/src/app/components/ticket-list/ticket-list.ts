import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketService } from '../../services/ticket';
import { Ticket } from '../../models/ticket.model';

@Component({
  selector: 'app-ticket-list',
  imports: [CommonModule],
  templateUrl: './ticket-list.html',
  styleUrl: './ticket-list.css',
})
export class TicketList implements OnInit {

  // La lista original con TODOS los tickets de la base de datos
  tickets = signal<Ticket[]>([]);

  // Guardamos cuál es el filtro actual. Por defecto es 'Todos'
  activeFilter = signal<string>('Todos');

  // LA MAGIA: Una lista computada que se filtra automáticamente
  // si 'tickets' o 'activeFilter' cambian.
  filteredTickets = computed(() => {
    const currentFilter = this.activeFilter();
    const allTickets = this.tickets();

    if (currentFilter === 'Todos') {
      return allTickets;
    }

    // Si no es 'Todos', filtramos donde la prioridad de la IA coincida con el botón
    return allTickets.filter(ticket => ticket.aiPriority === currentFilter);
  });

  private ticketService = inject(TicketService);

  ngOnInit(): void {
    this.loadTickets();
  }

  loadTickets(): void {
    this.ticketService.getAllTickets().subscribe({
      next: (data) => {
        this.tickets.set(data);
      },
      error: (error) => {
        console.error('Error al cargar tickets:', error);
      }
    });
  }

  // Función para cambiar el filtro cuando el usuario haga clic en un botón
  setFilter(priority: string) {
    this.activeFilter.set(priority);
  }
}