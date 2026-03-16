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

  // CORRECCIÓN: Ahora Spring devuelve un objeto con paginación, usamos 'any' (o una interfaz Page)
  getAllTickets(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
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
}
