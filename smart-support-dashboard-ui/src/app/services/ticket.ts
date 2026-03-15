import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ticket } from '../models/ticket.model'; // Importamos nuestro molde

// en esto de abajo se le dice a angular que este servicio es inyectable es decir que se conectará a este servicio cuando se necesite en el puerto 8080
@Injectable({
  providedIn: 'root',
})
export class TicketService {
  // La URL de tu servidor Java
  private apiUrl = 'https://smart-support-dashboard.onrender.com';

  // Inyectamos el "Postman" de Angular
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
}
