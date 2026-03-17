import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ticket } from '../models/ticket.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class TicketService {
  private apiUrl = `${environment.apiUrl}/tickets`;

  private http = inject(HttpClient);

  getAllTickets(page: number = 0, size: number = 10, filters: any = {}): Observable<any> {
    let params: any = { page, size };
    if (filters.title) params.title = filters.title;
    if (filters.status) params.status = filters.status;
    if (filters.priority) params.priority = filters.priority;
    if (filters.category) params.category = filters.category;
    
    return this.http.get<any>(this.apiUrl, { params });
  }

  // NUEVO: Método para enviar un ticket (POST)
  createTicket(ticket: Ticket): Observable<Ticket> {
    return this.http.post<Ticket>(this.apiUrl, ticket);
  }

  // NUEVO: Método para marcar como resuelto (PATCH)
  resolveTicket(id: number): Observable<Ticket> {
    // Mandamos un PATCH a la URL /api/tickets/{id}/resolve
    return this.http.patch<Ticket>(`${this.apiUrl}/${id}/resolve`, {});
  }

  // NUEVO: Traer un solo ticket por su ID (GET)
  getTicketById(id: number): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.apiUrl}/${id}`);
  }

  // NUEVO: Eliminar un ticket (DELETE)
  deleteTicket(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // NUEVO: Asignar ticket a un usuario
  assignTicket(id: number, username: string): Observable<Ticket> {
    return this.http.patch<Ticket>(`${this.apiUrl}/${id}/assign/${username}`, {});
  }

  // NUEVO: Obtener sugerencia de respuesta de la IA
  getAISuggestion(id: number): Observable<{ suggestion: string }> {
    return this.http.get<{ suggestion: string }>(`${this.apiUrl}/${id}/suggest-response`);
  }
}
