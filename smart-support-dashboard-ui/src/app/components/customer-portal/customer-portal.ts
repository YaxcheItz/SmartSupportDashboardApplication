import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TicketService } from '../../services/ticket';
import { AuthService } from '../../services/auth';
import { Ticket } from '../../models/ticket.model';

@Component({
  selector: 'app-customer-portal',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './customer-portal.html',
  styleUrl: './customer-portal.css'
})
export class CustomerPortalComponent implements OnInit {
  private ticketService = inject(TicketService);
  private authService = inject(AuthService);
  
  tickets: Ticket[] = [];
  isLoading = true;
  username = this.authService.getCurrentUser()?.username;

  ngOnInit() {
    this.loadMyTickets();
  }

  loadMyTickets() {
    this.isLoading = true;
    this.ticketService.getAllTickets(0, 50).subscribe({
      next: (response) => {
        this.tickets = response.content;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar tus tickets', error);
        this.isLoading = false;
      }
    });
  }

  getStatusClass(status: string | undefined): string {
    if (!status) return 'badge-default';
    switch (status) {
      case 'ABIERTO': return 'badge-open';
      case 'EN_PROGRESO': return 'badge-progress';
      case 'RESUELTO': return 'badge-resolved';
      default: return 'badge-default';
    }
  }
}
