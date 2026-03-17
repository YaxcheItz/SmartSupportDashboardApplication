import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TicketService } from '../../services/ticket';
import { AuthService } from '../../services/auth';
import { WebSocketService } from '../../services/websocket';
import { Ticket } from '../../models/ticket.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-customer-portal',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './customer-portal.html',
  styleUrl: './customer-portal.css'
})
export class CustomerPortalComponent implements OnInit, OnDestroy {
  private ticketService = inject(TicketService);
  private authService = inject(AuthService);
  private webSocketService = inject(WebSocketService);
  
  tickets = signal<Ticket[]>([]);
  isLoading = signal<boolean>(true);
  username = this.authService.getCurrentUser()?.username;
  private wsSubscription?: Subscription;

  ngOnInit() {
    this.loadMyTickets();
    
    // Conectar a WebSockets para actualizaciones en vivo
    this.webSocketService.connect();
    this.wsSubscription = this.webSocketService.ticketUpdates$.subscribe(updatedTicket => {
      console.log('Cliente recibió actualización:', updatedTicket);
      
      this.tickets.update(currentTickets => {
        const index = currentTickets.findIndex(t => t.id === updatedTicket.id);
        
        if (index !== -1) {
          // Si el ticket ya existe, lo actualizamos (ej: pasó de ABIERTO a RESUELTO o la IA terminó)
          const newList = [...currentTickets];
          newList[index] = updatedTicket;
          return newList;
        } else {
          // Si es un ticket nuevo, comprobar si pertenece al usuario actual antes de añadirlo
          const currentUserEmail = this.authService.getCurrentUser()?.email;
          if (updatedTicket.customerEmail === currentUserEmail) {
            return [updatedTicket, ...currentTickets];
          }
          return currentTickets;
        }
      });
    });
  }

  ngOnDestroy() {
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
    }
    this.webSocketService.disconnect();
  }

  loadMyTickets() {
    this.isLoading.set(true);
    this.ticketService.getAllTickets(0, 50).subscribe({
      next: (response) => {
        this.tickets.set(response.content || []);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error al cargar tus tickets', error);
        this.tickets.set([]);
        this.isLoading.set(false);
      }
    });
  }

  getStatusClass(status: string | undefined): string {
    if (!status) return 'badge-default';
    switch (status) {
      case 'ABIERTO': return 'badge-open';
      case 'EN_PROGRESO': return 'badge-progress';
      case 'RESUELTO': return 'badge-resolved';
      case 'CERRADO': return 'badge-resolved';
      default: return 'badge-default';
    }
  }
}
