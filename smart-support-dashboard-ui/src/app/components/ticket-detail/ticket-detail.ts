import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TicketService } from '../../services/ticket';
import { Ticket } from '../../models/ticket.model';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ticket-detail.html',
  styleUrls: ['./ticket-detail.css']
})
export class TicketDetailComponent implements OnInit {

  // Signal para guardar el ticket o null mientras carga
  ticket = signal<Ticket | null>(null);
  loading = signal<boolean>(true);

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private ticketService = inject(TicketService);

  ngOnInit(): void {
    // 1. Obtener el ID de la URL
    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam) {
      this.loadTicketDetails(Number(idParam));
    } else {
      this.goBack(); // Si no hay ID válido, lo devolvemos
    }
  }

  loadTicketDetails(id: number) {
    this.ticketService.getTicketById(id).subscribe({
      next: (data) => {
        this.ticket.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando ticket', err);
        this.loading.set(false);
      }
    });
  }

  goBack() {
    this.router.navigate(['/dashboard']);
  }
}