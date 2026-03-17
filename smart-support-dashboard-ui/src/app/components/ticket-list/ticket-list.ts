import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketService } from '../../services/ticket';
import { Ticket } from '../../models/ticket.model';
import { ToastService } from '../../services/toast';
import { AuthService } from '../../services/auth';
import { DashboardStatsComponent } from '../dashboard-stats/dashboard-stats';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WebSocketService } from '../../services/websocket';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-ticket-list',
  imports: [CommonModule, DashboardStatsComponent, RouterModule, FormsModule],
  templateUrl: './ticket-list.html',
  styleUrl: './ticket-list.css',
})
export class TicketList implements OnInit, OnDestroy {
  tickets = signal<Ticket[]>([]);
  isLoading = signal<boolean>(true);
  isWakingUp = signal<boolean>(false);
  
  // Filtros Avanzados
  filterValues = {
    title: '',
    status: '',
    priority: '',
    category: ''
  };
  private searchTimeout: any;
  
  // Paginación
  currentPage = signal<number>(0);
  totalPages = signal<number>(0);
  pageSize = 10;

  private ticketService = inject(TicketService);
  private toastService = inject(ToastService);
  private authService = inject(AuthService);
  private webSocketService = inject(WebSocketService);
  private wsSubscription?: Subscription;

  isAdmin = computed(() => this.authService.isAdmin());

  ngOnInit(): void {
    this.loadTickets(0);
    
    // Conectar a WebSockets y escuchar actualizaciones
    this.webSocketService.connect();
    this.wsSubscription = this.webSocketService.ticketUpdates$.subscribe(updatedTicket => {
      // Si el ticket actualizado coincide con los filtros actuales (o es una actualización simple)
      this.tickets.update(currentTickets => {
        const index = currentTickets.findIndex(t => t.id === updatedTicket.id);
        if (index !== -1) {
          const newList = [...currentTickets];
          newList[index] = updatedTicket;
          if (currentTickets[index].aiCategory === 'Analizando...' && updatedTicket.aiCategory !== 'Analizando...') {
             this.toastService.showSuccess(`La IA ha terminado de analizar el ticket #${updatedTicket.id}`);
          }
          return newList;
        }
        return currentTickets;
      });
    });
  }

  ngOnDestroy(): void {
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
    }
  }

  // Método para aplicar filtros con retraso (debounce)
  onFilterChange() {
    if (this.searchTimeout) clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.loadTickets(0);
    }, 400);
  }

  loadTickets(page: number): void {
    this.isLoading.set(true);
    this.ticketService.getAllTickets(page, this.pageSize, this.filterValues).subscribe({
      next: (data) => {
        this.tickets.set(data.content || []);
        this.currentPage.set(data.number);
        this.totalPages.set(data.totalPages);
        this.isLoading.set(false);
        this.isWakingUp.set(false);
      },
      error: (error) => {
        this.isLoading.set(false);
        if (error.status === 0) {
          this.isWakingUp.set(true);
          setTimeout(() => this.loadTickets(page), 5000);
        } else {
          console.error('Error al cargar tickets:', error);
        }
      }
    });
  }

  nextPage() {
    if (this.currentPage() < this.totalPages() - 1) {
      this.loadTickets(this.currentPage() + 1);
    }
  }

  prevPage() {
    if (this.currentPage() > 0) {
      this.loadTickets(this.currentPage() - 1);
    }
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

  assignToMe(event: Event, id: number | undefined) {
    event.stopPropagation();
    const user = this.authService.getCurrentUser();
    if (!id || !user) return;

    this.ticketService.assignTicket(id, user.username).subscribe({
      next: (res) => {
        this.tickets.update(list => list.map(t => t.id === id ? res : t));
        this.toastService.showSuccess('Ticket asignado correctamente');
      }
    });
  }
}
