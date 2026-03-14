import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketService } from '../../services/ticket';
import { Ticket } from '../../models/ticket.model';
import { ToastService } from '../../services/toast'; // <-- IMPORTANTE IMPORTARLO
import { DashboardStatsComponent } from '../dashboard-stats/dashboard-stats';
import { RouterModule } from '@angular/router';
@Component({
  selector: 'app-ticket-list',
  imports: [CommonModule, DashboardStatsComponent, RouterModule],
  templateUrl: './ticket-list.html',
  styleUrl: './ticket-list.css',
})
export class TicketList implements OnInit {

  tickets = signal<Ticket[]>([]);
  activeFilter = signal<string>('Todos');

  filteredTickets = computed(() => {
    const currentFilter = this.activeFilter();
    const allTickets = this.tickets();

    if (currentFilter === 'Todos') return allTickets;
    return allTickets.filter(ticket => ticket.aiPriority === currentFilter);
  });

  private ticketService = inject(TicketService);
  private toastService = inject(ToastService); // <-- INYECTARLO AQUÍ

  ngOnInit(): void {
    this.loadTickets();
  }

  loadTickets(): void {
    this.ticketService.getAllTickets().subscribe({
      next: (data) => {
        // CORRECCIÓN: Spring mete la lista dentro de "content" por la paginación
        this.tickets.set(data.content || []);
      },
      error: (error) => console.error('Error al cargar tickets:', error)
    });
  }

  setFilter(priority: string) {
    this.activeFilter.set(priority);
  }

  // NUEVO: Método que se ejecuta al presionar el botón
  resolveTicket(id: number | undefined) {
    if (!id) return;

    this.ticketService.resolveTicket(id).subscribe({
      next: (resolvedTicket) => {
        // Actualizamos la lista local (Signal) sin recargar toda la página
        this.tickets.update(currentTickets =>
          currentTickets.map(t => t.id === id ? resolvedTicket : t)
        );

        this.toastService.showSuccess('¡Ticket resuelto con éxito!');
      },
      error: (error) => {
        this.toastService.showError('Error al cerrar el ticket');
        console.error(error);
      }
    });
  }
}