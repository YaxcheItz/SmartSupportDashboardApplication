import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketService } from '../../services/ticket';
import { Ticket } from '../../models/ticket.model';
import { ToastService } from '../../services/toast';
import { DashboardStatsComponent } from '../dashboard-stats/dashboard-stats';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-ticket-list',
  imports: [CommonModule, DashboardStatsComponent, RouterModule, FormsModule],
  templateUrl: './ticket-list.html',
  styleUrl: './ticket-list.css',
})
export class TicketList implements OnInit {
  tickets = signal<Ticket[]>([]);
  activeFilter = signal<string>('Todos');
  searchQuery = signal<string>('');

  // Contador de tickets por prioridad (para los filtros)
  counts = computed(() => {
    const all = this.tickets();
    return {
      Todos: all.length,
      Urgente: all.filter(t => t.aiPriority === 'Urgente').length,
      Alta: all.filter(t => t.aiPriority === 'Alta').length,
      Media: all.filter(t => t.aiPriority === 'Media').length,
      Baja: all.filter(t => t.aiPriority === 'Baja').length,
    };
  });

  filteredTickets = computed(() => {
    const filter = this.activeFilter();
    const query = this.searchQuery().toLowerCase();
    
    return this.tickets().filter(t => {
      const matchesFilter = filter === 'Todos' || t.aiPriority === filter;
      const matchesQuery = (t.title?.toLowerCase() || '').includes(query) || 
                           (t.customerEmail?.toLowerCase() || '').includes(query);
      return matchesFilter && matchesQuery;
    });
  });

  private ticketService = inject(TicketService);
  private toastService = inject(ToastService);

  ngOnInit(): void { this.loadTickets(); }

  loadTickets() {
    this.ticketService.getAllTickets().subscribe({
      next: (data) => this.tickets.set(data.content || []),
      error: (err) => console.error('Error al cargar:', err)
    });
  }

  setFilter(priority: string) { this.activeFilter.set(priority); }

  resolveTicket(event: Event, id: number | undefined) {
    event.stopPropagation(); // Evita que el click abra los detalles
    if (!id) return;
    this.ticketService.resolveTicket(id).subscribe({
      next: (res) => {
        this.tickets.update(list => list.map(t => t.id === id ? res : t));
        this.toastService.showSuccess('Ticket marcado como resuelto');
      }
    });
  }

  deleteTicket(event: Event, id: number | undefined) {
    event.stopPropagation(); // ¡CLAVE PARA QUE FUNCIONE!
    if (!id) return;

    if (confirm('¿Eliminar este ticket permanentemente?')) {
      this.ticketService.deleteTicket(id).subscribe({
        next: () => {
          this.tickets.update(list => list.filter(t => t.id !== id));
          this.toastService.showSuccess('Ticket eliminado');
        },
        error: (err) => {
          console.error('Error al borrar:', err);
          this.toastService.showError('No se pudo borrar del servidor');
        }
      });
    }
  }
}
