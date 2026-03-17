import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TicketService } from '../../services/ticket';
import { AuthService } from '../../services/auth';
import { ToastService } from '../../services/toast';
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
  
  // IA Suggestion
  aiSuggestion = signal<string | null>(null);
  isGeneratingSuggestion = signal<boolean>(false);
  isAdmin = inject(AuthService).isAdmin();

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private ticketService = inject(TicketService);
  private toastService = inject(ToastService);

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

  generateSuggestion() {
    const t = this.ticket();
    if (!t || !t.id) return;

    this.isGeneratingSuggestion.set(true);
    this.aiSuggestion.set(null);

    this.ticketService.getAISuggestion(t.id).subscribe({
      next: (response) => {
        this.aiSuggestion.set(response.suggestion);
        this.isGeneratingSuggestion.set(false);
        this.toastService.showSuccess('Respuesta sugerida generada');
      },
      error: (err) => {
        this.isGeneratingSuggestion.set(false);
        this.toastService.showError('Error al generar sugerencia');
      }
    });
  }

  copyToClipboard() {
    const suggestion = this.aiSuggestion();
    if (suggestion) {
      navigator.clipboard.writeText(suggestion);
      this.toastService.showSuccess('Copiado al portapapeles');
    }
  }

  goBack() {
    const isAdmin = inject(AuthService).isAdmin();
    this.router.navigate([isAdmin ? '/dashboard' : '/portal']);
  }
}