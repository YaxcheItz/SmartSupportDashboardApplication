import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TicketService } from '../../services/ticket';
import { Ticket } from '../../models/ticket.model';

@Component({
  selector: 'app-ticket-form',
  // Importamos ReactiveFormsModule para que los formularios de Angular funcionen
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './ticket-form.html',
  styleUrl: './ticket-form.css'
})
export class TicketForm {
  ticketForm: FormGroup; // El grupo que controlará todos nuestros inputs
  isSubmitting = false;  // Variable para saber si la IA está "pensando"

  private fb = inject(FormBuilder);
  private ticketService = inject(TicketService);
  private router = inject(Router); // Para redirigir al usuario cuando termine

  constructor() {
    // Aquí definimos las reglas estrictas de nuestro formulario
    this.ticketForm = this.fb.group({
      customerEmail: ['', [Validators.required, Validators.email]], // Debe ser obligatorio y tener @
      title: ['', [Validators.required, Validators.minLength(5)]], // Obligatorio, mínimo 5 letras
      description: ['', [Validators.required, Validators.minLength(15)]] // Obligatorio, mínimo 15 letras
    });
  }

  // Método que se dispara al darle al botón "Enviar"
  onSubmit(): void {
    // Si el formulario es inválido, no hacemos nada
    if (this.ticketForm.invalid) {
      return;
    }

    // Ponemos la variable en true para mostrar el "spinner" o mensaje de carga
    this.isSubmitting = true;

    // Extraemos los datos que escribió el usuario
    const newTicket: Ticket = this.ticketForm.value;

    // Se lo mandamos al Java (y Java se lo mandará a la IA)
    this.ticketService.createTicket(newTicket).subscribe({
      next: (response) => {
        console.log('Ticket creado con IA:', response);
        this.isSubmitting = false; // Apagamos el mensaje de carga
        // Redirigimos al usuario automáticamente de vuelta a la lista para que vea su tarjeta
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.error('Error al crear ticket', err);
        this.isSubmitting = false;
        alert('Hubo un error al enviar el ticket. Revisa que el backend esté corriendo.');
      }
    });
  }
}