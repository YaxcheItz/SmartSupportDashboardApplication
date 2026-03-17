import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { TicketService } from '../../services/ticket';
import { ToastService } from '../../services/toast';
import { AuthService } from '../../services/auth';
import { Router } from '@angular/router';

@Component({
  selector: 'app-ticket-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './ticket-form.html',
  styleUrls: ['./ticket-form.css'],
})
export class TicketForm {
  ticketForm: FormGroup;
  isSubmitting = false;

  private fb = inject(FormBuilder);
  private ticketService = inject(TicketService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  constructor() {
    const user = this.authService.getCurrentUser();
    this.ticketForm = this.fb.group({
      customerEmail: [user?.email || '', [Validators.required, Validators.email]],
      title: ['', [Validators.required, Validators.minLength(5)]],
      description: ['', [Validators.required, Validators.minLength(15)]],
    });
  }

  onSubmit() {
    if (this.ticketForm.valid) {
      this.isSubmitting = true;
      this.cdr.detectChanges();

      this.ticketService.createTicket(this.ticketForm.value).subscribe({
        next: (response) => {
          this.toastService.showSuccess('Ticket creado y analizado por la IA');
          const isStaff = this.authService.isStaff();
          this.router.navigate([isStaff ? '/dashboard' : '/portal']);
        },
        error: (error) => {
          this.isSubmitting = false;
          this.cdr.detectChanges();
          this.toastService.showError('Error al crear el ticket. Revisa la conexión.');
          console.error('Error al crear ticket', error);
        },
      });
    }
  }
}
