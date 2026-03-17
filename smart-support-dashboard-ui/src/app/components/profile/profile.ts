import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../services/user';
import { TicketService } from '../../services/ticket';
import { ToastService } from '../../services/toast';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class ProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private ticketService = inject(TicketService);
  private toastService = inject(ToastService);
  authService = inject(AuthService);

  profileForm: FormGroup;
  isLoading = signal(false);
  isUploading = signal(false);
  user = this.authService.currentUser;

  constructor() {
    this.profileForm = this.fb.group({
      username: [{ value: '', disabled: true }],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.minLength(6)]]
    });
  }

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    const currentUser = this.user();
    if (currentUser) {
      this.profileForm.patchValue({
        username: currentUser.username,
        email: currentUser.email
      });
    }
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 2 * 1024 * 1024) {
        this.toastService.showError('La imagen es muy pesada. Máximo 2MB.');
        return;
      }

      this.isUploading.set(true);
      this.ticketService.uploadFile(file).subscribe({
        next: (res) => {
          // Actualizar solo el avatarUrl
          this.userService.updateProfile({ avatarUrl: res.url }).subscribe({
            next: () => {
              // Actualizar signal global
              const currentUser = this.authService.currentUser();
              if (currentUser) {
                this.authService.currentUser.set({ ...currentUser, avatarUrl: res.url });
                localStorage.setItem('avatarUrl', res.url);
              }
              this.isUploading.set(false);
              this.toastService.showSuccess('Foto de perfil actualizada');
            },
            error: () => {
              this.isUploading.set(false);
              this.toastService.showError('Error al guardar la foto');
            }
          });
        },
        error: () => {
          this.isUploading.set(false);
          this.toastService.showError('Error al subir la imagen');
        }
      });
    }
  }

  onSubmit() {
    if (this.profileForm.valid) {
      this.isLoading.set(true);
      const data: any = {
        email: this.profileForm.value.email
      };

      if (this.profileForm.value.password) {
        data.password = this.profileForm.value.password;
      }

      this.userService.updateProfile(data).subscribe({
        next: () => {
          this.toastService.showSuccess('Información actualizada correctamente');
          this.isLoading.set(false);
          this.profileForm.get('password')?.reset();
        },
        error: (err) => {
          this.toastService.showError('Error al actualizar');
          this.isLoading.set(false);
          console.error(err);
        }
      });
    }
  }
}
