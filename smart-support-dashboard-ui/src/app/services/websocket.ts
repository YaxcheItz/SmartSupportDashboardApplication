import { Injectable, inject } from '@angular/core';
import { Client, Message, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';
import { environment } from '../../environments/environment';
import { Ticket } from '../models/ticket.model';
import { AuthService } from './auth';

export interface TypingIndicator {
  ticketId: number;
  username: string;
  isTyping: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client;
  private ticketUpdatesSource = new Subject<Ticket>();
  private typingIndicatorSource = new Subject<TypingIndicator>();
  
  public ticketUpdates$ = this.ticketUpdatesSource.asObservable();
  public typingIndicator$ = this.typingIndicatorSource.asObservable();
  
  private authService = inject(AuthService);
  private currentSubscriptions: Map<string, StompSubscription> = new Map();
  private isConnected = false;
  private pendingSubscriptions: (() => void)[] = [];

  constructor() {
    const isSecure = window.location.protocol === 'https:';

    this.client = new Client({
      webSocketFactory: isSecure ? undefined : () => {
        const baseUrl = environment.apiUrl.replace('/api', ''); 
        return new SockJS(`${baseUrl}/ws-tickets`);
      },
      brokerURL: isSecure
        ? environment.apiUrl.replace('https://', 'wss://').replace('/api', '/ws-tickets/websocket')
        : undefined,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = (frame) => {
      console.log('¡Conectado a WebSockets!', frame);
      this.isConnected = true;
      
      this.client.subscribe('/topic/tickets', (message: Message) => {
        if (message.body) {
          const updatedTicket: Ticket = JSON.parse(message.body);
          this.ticketUpdatesSource.next(updatedTicket);
        }
      });

      // Ejecutar suscripciones que estaban esperando a que el túnel se abriera
      this.pendingSubscriptions.forEach(subFn => subFn());
      this.pendingSubscriptions = [];
    };

    this.client.onDisconnect = () => {
      this.isConnected = false;
    };

    this.client.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      this.isConnected = false;
    };
  }

  connect() {
    if (!this.client.active) {
      const token = this.authService.getToken();
      if (token) {
        this.client.connectHeaders = {
          'Authorization': `Bearer ${token}`
        };
      }
      this.client.activate();
    }
  }

  disconnect() {
    if (this.client.active) {
      this.client.deactivate();
      this.currentSubscriptions.clear();
      this.isConnected = false;
    }
  }

  // Permite suscribirse a los eventos de escritura de un ticket específico
  subscribeToTypingIndicators(ticketId: number) {
    const subKey = `typing-${ticketId}`;
    if (this.currentSubscriptions.has(subKey)) return;

    const doSubscribe = () => {
      const subscription = this.client.subscribe(`/topic/ticket/${ticketId}/typing`, (message: Message) => {
        if (message.body) {
          const indicator: TypingIndicator = JSON.parse(message.body);
          this.typingIndicatorSource.next(indicator);
        }
      });
      this.currentSubscriptions.set(subKey, subscription);
    };

    if (this.isConnected) {
      doSubscribe();
    } else {
      // Guardar en cola si aún no estamos conectados
      this.pendingSubscriptions.push(doSubscribe);
      if (!this.client.active) this.connect();
    }
  }

  unsubscribeFromTypingIndicators(ticketId: number) {
    const subKey = `typing-${ticketId}`;
    const subscription = this.currentSubscriptions.get(subKey);
    if (subscription) {
      subscription.unsubscribe();
      this.currentSubscriptions.delete(subKey);
    }
  }

  // Envía el evento de escritura al backend
  sendTypingIndicator(ticketId: number, username: string, isTyping: boolean) {
    if (this.client.active) {
      this.client.publish({
        destination: `/app/ticket/${ticketId}/typing`,
        body: JSON.stringify({ ticketId, username, isTyping })
      });
    }
  }
}

