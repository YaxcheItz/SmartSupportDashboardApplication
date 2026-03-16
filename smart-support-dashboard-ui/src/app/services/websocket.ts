import { Injectable, inject } from '@angular/core';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';
import { environment } from '../../environments/environment';
import { Ticket } from '../models/ticket.model';
import { AuthService } from './auth';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client;
  private ticketUpdatesSource = new Subject<Ticket>();
  
  // Observable al que los componentes pueden suscribirse para escuchar cambios
  public ticketUpdates$ = this.ticketUpdatesSource.asObservable();
  
  private authService = inject(AuthService);

  constructor() {
    this.client = new Client({
      // En desarrollo usamos SockJS, en producción usamos WebSockets nativos (wss://)
      webSocketFactory: environment.production ? undefined : () => {
        const baseUrl = environment.apiUrl.replace('/api', ''); 
        return new SockJS(`${baseUrl}/ws-tickets`);
      },
      brokerURL: environment.production
        ? environment.apiUrl.replace('https://', 'wss://').replace('/api', '/ws-tickets')
        : undefined, // Si no es prod, se usa el webSocketFactory de arriba
      debug: (str) => {
        // Descomenta esto para ver los logs de WebSocket si falla algo
        // console.log(str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = (frame) => {
      console.log('¡Conectado a WebSockets!', frame);
      
      // Suscribirse al canal de tickets actualizados por IA
      this.client.subscribe('/topic/tickets', (message: Message) => {
        if (message.body) {
          const updatedTicket: Ticket = JSON.parse(message.body);
          this.ticketUpdatesSource.next(updatedTicket); // Emitir el evento
        }
      });
    };

    this.client.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };
  }

  // Llamar a este método cuando el usuario inicie sesión o el dashboard cargue
  connect() {
    if (!this.client.active) {
      this.client.activate();
    }
  }

  // Llamar al cerrar sesión
  disconnect() {
    if (this.client.active) {
      this.client.deactivate();
    }
  }
}
