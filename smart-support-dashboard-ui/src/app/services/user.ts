import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  getProfile(): Observable<any> {
    return this.http.get(`${this.apiUrl}/me`);
  }

  updateProfile(data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/me`, data).pipe(
      tap(() => {
        // Actualizar datos locales si el email cambió
        if (data.email) {
          localStorage.setItem('email', data.email);
          const currentUser = this.authService.currentUser();
          if (currentUser) {
            this.authService.currentUser.set({ ...currentUser, email: data.email });
          }
        }
      })
    );
  }
}
