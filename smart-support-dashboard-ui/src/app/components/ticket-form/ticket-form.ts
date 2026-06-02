import { Component, inject, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
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
import { Subject, Subscription, debounceTime, distinctUntilChanged, switchMap, filter, of, forkJoin } from 'rxjs';

interface FileWithPreview {
  file: File;
  preview: string | null;
}

@Component({
  selector: 'app-ticket-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './ticket-form.html',
  styleUrls: ['./ticket-form.css'],
})
export class TicketForm implements OnInit, OnDestroy {
  ticketForm: FormGroup;
  isSubmitting = false;
  selectedFiles: FileWithPreview[] = [];

  // IA Prevención
  quickSolution: string | null = null;
  isSearchingSolution = false;
  private titleSubject = new Subject<string>();
  private titleSubscription?: Subscription;

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

  ngOnInit() {
    // Escuchar cambios en el título
    this.ticketForm.get('title')?.valueChanges.subscribe(value => {
      this.quickSolution = null; // Limpiar sugerencia anterior
      if (value && value.length >= 10) {
        this.titleSubject.next(value);
      }
    });

    // Procesar búsqueda con IA
    this.titleSubscription = this.titleSubject.pipe(
      debounceTime(800), // Esperar a que deje de escribir
      distinctUntilChanged(),
      filter(title => title.length >= 10),
      switchMap(title => {
        this.isSearchingSolution = true;
        this.cdr.detectChanges();
        return this.ticketService.getQuickSolution(title);
      })
    ).subscribe({
      next: (res) => {
        this.isSearchingSolution = false;
        if (res && res.suggestion) {
          this.quickSolution = res.suggestion;
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.isSearchingSolution = false;
        this.cdr.detectChanges();
      }
    });
  }

  ngOnDestroy() {
    if (this.titleSubscription) {
      this.titleSubscription.unsubscribe();
    }
  }

  onFileSelected(event: any) {
    const files: FileList = event.target.files;
    if (files && files.length > 0) {
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        if (file.size > 5 * 1024 * 1024) {
          this.toastService.showError(`"${file.name}" es demasiado grande. Máximo 5MB.`);
          continue;
        }

        const fileObj: FileWithPreview = { file, preview: null };
        this.selectedFiles.push(fileObj);

        // Generar vista previa si es imagen
        if (file.type.startsWith('image/')) {
          const reader = new FileReader();
          reader.onload = e => fileObj.preview = reader.result as string;
          reader.readAsDataURL(file);
        }
      }
    }
  }

  removeFile(index: number) {
    this.selectedFiles.splice(index, 1);
  }

  onSubmit() {
    if (this.ticketForm.invalid) return;
    
    this.isSubmitting = true;
    this.cdr.detectChanges();

    if (this.selectedFiles.length > 0) {
      // Subir todos los archivos en paralelo usando forkJoin
      const uploadTasks = this.selectedFiles.map(f => this.ticketService.uploadFile(f.file));
      
      forkJoin(uploadTasks).subscribe({
        next: (responses) => {
          // Extraemos los nombres de archivos (ahora el backend devuelve solo el path/nombre)
          const fileNames = responses.map(res => res.url); 
          this.saveTicket(fileNames);
        },
        error: (err) => {
          this.isSubmitting = false;
          this.cdr.detectChanges();
          this.toastService.showError('Error al subir los archivos adjuntos.');
          console.error('Error subiendo archivos', err);
        }
      });
    } else {
      this.saveTicket([]);
    }
  }

  private saveTicket(attachmentFileNames: string[]) {
    const ticketData = { 
      ...this.ticketForm.value,
      attachmentFileNames: attachmentFileNames
    };

    this.ticketService.createTicket(ticketData).subscribe({
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
