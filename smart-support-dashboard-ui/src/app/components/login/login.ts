import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';
import { ToastService } from '../../services/toast';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class LoginComponent {
  loginForm: FormGroup;
  registerForm: FormGroup;
  isLoading = signal<boolean>(false);
  isLoginMode = signal<boolean>(true);
  errorMessage = signal<string | null>(null);

  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  constructor() {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });

    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  toggleMode() {
    this.isLoginMode.set(!this.isLoginMode());
    this.errorMessage.set(null);
    this.loginForm.reset();
    this.registerForm.reset();
  }

  onLogin() {
    if (this.loginForm.valid) {
      this.isLoading.set(true);
      this.errorMessage.set(null);
      const { username, password } = this.loginForm.value;

      this.authService.login(username, password).subscribe({
        next: (res) => {
          this.isLoading.set(false);
          this.toastService.showSuccess('¡Bienvenido Agente!');
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set('Usuario o contraseña incorrectos');
          this.toastService.showError('Fallo en el inicio de sesión');
          console.error(err);
        },
      });
    }
  }

  onRegister() {
    if (this.registerForm.valid) {
      this.isLoading.set(true);
      this.errorMessage.set(null);
      const { username, email, password } = this.registerForm.value;

      this.authService.register(username, email, password).subscribe({
        next: () => {
          this.isLoading.set(false);
          this.toastService.showSuccess('Registro exitoso. Por favor inicia sesión.');
          this.toggleMode(); // Cambia al modo login
        },
        error: (err) => {
          this.isLoading.set(false);
          let msg = 'Error en el registro';
          if (err.error && err.error.message) {
            if (err.error.message.includes('Username')) msg = 'El nombre de usuario ya está en uso';
            else if (err.error.message.includes('Email')) msg = 'El correo electrónico ya está en uso';
            else msg = err.error.message;
          }
          this.errorMessage.set(msg);
          this.toastService.showError(msg);
          console.error(err);
        },
      });
    }
  }
}