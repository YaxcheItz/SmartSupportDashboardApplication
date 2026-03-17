import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TicketService } from '../../services/ticket';
import { AuthService } from '../../services/auth';
import { ToastService } from '../../services/toast';
import { Ticket } from '../../models/ticket.model';
import { Comment } from '../../models/comment.model';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './ticket-detail.html',
  styleUrls: ['./ticket-detail.css']
})
export class TicketDetailComponent implements OnInit {

  ticket = signal<Ticket | null>(null);
  loading = signal<boolean>(true);

  aiSuggestion = signal<string | null>(null);
  isGeneratingSuggestion = signal<boolean>(false);
  isAdmin = inject(AuthService).isAdmin();
  isStaff = inject(AuthService).isStaff();
  currentUser = inject(AuthService).getCurrentUser();

  comments = signal<Comment[]>([]);
  commentForm: FormGroup;
  isSubmittingComment = signal<boolean>(false);

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private ticketService = inject(TicketService);
  private toastService = inject(ToastService);
  private fb = inject(FormBuilder);
  authService = inject(AuthService);

  constructor() {
    this.commentForm = this.fb.group({
      content: ['', [Validators.required, Validators.minLength(2)]]
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.loadTicketDetails(Number(idParam));
      this.loadComments(Number(idParam));
    } else {
      this.goBack();
    }
  }

  loadTicketDetails(id: number) {
    this.ticketService.getTicketById(id).subscribe({
      next: (data) => {
        this.ticket.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando ticket', err);
        this.loading.set(false);
      }
    });
  }

  loadComments(ticketId: number) {
    this.ticketService.getComments(ticketId).subscribe({
      next: (data) => this.comments.set(data),
      error: (err) => console.error('Error cargando comentarios', err)
    });
  }

  addComment() {
    if (this.commentForm.invalid) return;

    const t = this.ticket();
    if (!t || !t.id) return;

    this.isSubmittingComment.set(true);
    const content = this.commentForm.value.content;

    this.ticketService.addComment(t.id, content).subscribe({
      next: (newComment) => {
        this.comments.update(list => [...list, newComment]);
        this.commentForm.reset();
        this.isSubmittingComment.set(false);
        this.toastService.showSuccess('Comentario añadido');
      },
      error: (err) => {
        console.error(err);
        this.isSubmittingComment.set(false);
        this.toastService.showError('No se pudo enviar el comentario');
      }
    });
  }

  generateSuggestion() {
    const t = this.ticket();
    if (!t || !t.id) return;

    this.isGeneratingSuggestion.set(true);
    this.aiSuggestion.set(null);

    this.ticketService.getAISuggestion(t.id).subscribe({
      next: (response) => {
        this.aiSuggestion.set(response.suggestion);
        this.isGeneratingSuggestion.set(false);
        this.toastService.showSuccess('Respuesta sugerida generada');
      },
      error: (err) => {
        this.isGeneratingSuggestion.set(false);
        this.toastService.showError('Error al generar sugerencia');
      }
    });
  }

  copyToClipboard() {
    const suggestion = this.aiSuggestion();
    if (suggestion) {
      navigator.clipboard.writeText(suggestion);
      this.toastService.showSuccess('Copiado al portapapeles');
    }
  }

  goBack() {
    this.router.navigate([this.isStaff ? '/dashboard' : '/portal']);
  }
}