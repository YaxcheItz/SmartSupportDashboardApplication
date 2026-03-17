import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ticket } from '../models/ticket.model';
import { Comment } from '../models/comment.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class TicketService {
  private apiUrl = `${environment.apiUrl}/tickets`;

  private http = inject(HttpClient);

  // ... (otros métodos existentes)

  getAllTickets(page: number = 0, size: number = 10, filters: any = {}): Observable<any> {
    let params: any = { page, size };
    if (filters.title) params.title = filters.title;
    if (filters.status) params.status = filters.status;
    if (filters.priority) params.priority = filters.priority;
    if (filters.category) params.category = filters.category;

    return this.http.get<any>(this.apiUrl, { params });
  }

  createTicket(ticket: Ticket): Observable<Ticket> {
    return this.http.post<Ticket>(this.apiUrl, ticket);
  }

  resolveTicket(id: number): Observable<Ticket> {
    return this.http.patch<Ticket>(`${this.apiUrl}/${id}/resolve`, {});
  }

  getTicketById(id: number): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.apiUrl}/${id}`);
  }

  deleteTicket(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  assignTicket(id: number, username: string): Observable<Ticket> {
    return this.http.patch<Ticket>(`${this.apiUrl}/${id}/assign/${username}`, {});
  }

  getAISuggestion(id: number): Observable<{ suggestion: string }> {
    return this.http.get<{ suggestion: string }>(`${this.apiUrl}/${id}/suggest-response`);
  }

  // --- NUEVOS MÉTODOS PARA COMENTARIOS ---

  getComments(ticketId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.apiUrl}/${ticketId}/comments`);
  }

  addComment(ticketId: number, content: string): Observable<Comment> {
    return this.http.post<Comment>(`${this.apiUrl}/${ticketId}/comments`, { content });
  }
}
