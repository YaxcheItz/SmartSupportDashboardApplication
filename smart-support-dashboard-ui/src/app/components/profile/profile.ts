import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../services/user';
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
  private toastService = inject(ToastService);
  authService = inject(AuthService);

  profileForm: FormGroup;
  isLoading = signal(false);
  user = this.authService.currentUser;

  constructor() {
    this.profileForm = this.fb.group({
      username: [{ value: '', disabled: true }],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.minLength(6)]]
    });
  }

  ngOnInit() {
    const currentUser = this.user();
    if (currentUser) {
      this.profileForm.patchValue({
        username: currentUser.username,
        email: currentUser.email
      });
    }
  }

  onSubmit() {
    if (this.profileForm.valid) {
      this.isLoading.set(true);
      const data = {
        email: this.profileForm.value.email,
        password: this.profileForm.value.password
      };

      // Si el password está vacío, no lo enviamos
      if (!data.password) delete data.password;

      this.userService.updateProfile(data).subscribe({
        next: () => {
          this.toastService.showSuccess('Perfil actualizado correctamente');
          this.isLoading.set(false);
          this.profileForm.get('password')?.reset();
        },
        error: (err) => {
          this.toastService.showError('Error al actualizar el perfil');
          this.isLoading.set(false);
          console.error(err);
        }
      });
    }
  }
}
