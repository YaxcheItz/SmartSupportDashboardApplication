export interface Ticket {
  id?: number; // El signo de interrogación significa que es opcional (cuando lo creamos no tiene ID aún)
  title: string;
  description: string;
  customerEmail: string;
  aiCategory?: string;
  aiPriority?: string;
  aiTone?: string;
  aiSummary?: string;
  createdAt?: string;
  status?: string;
}
//tengo los mismos campos que en java y los mismos nombres
