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
  private apiUrl = 'http://localhost:8080/api/tickets';

  // Inyectamos el "Postman" de Angular
  private http = inject(HttpClient);

  // Método para traer todos los tickets (GET)
  getAllTickets(): Observable<Ticket[]> {
    // Le decimos: "Ve a la URL y devuélveme una lista de Tickets"
    return this.http.get<Ticket[]>(this.apiUrl);
  }

  // Método para enviar un nuevo ticket (POST)
  createTicket(ticket: Ticket): Observable<Ticket> {
    return this.http.post<Ticket>(this.apiUrl, ticket);
  }
}

