import { Injectable, signal } from '@angular/core';

export interface ToastMessage {
  message: string;
  type: 'success' | 'error';
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {

  // El signal guarda el mensaje actual (o null si no hay ninguno)
  currentToast = signal<ToastMessage | null>(null);

  showSuccess(message: string) {
    this.currentToast.set({ message, type: 'success' });
    this.autoHide();
  }

  showError(message: string) {
    this.currentToast.set({ message, type: 'error' });
    this.autoHide();
  }

  // Desaparece automáticamente después de 3 segundos
  private autoHide() {
    setTimeout(() => {
      this.currentToast.set(null);
    }, 3000);
  }
}