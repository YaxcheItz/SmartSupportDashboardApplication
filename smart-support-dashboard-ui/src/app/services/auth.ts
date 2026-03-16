import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface User {
  id?: number;
  username: string;
  email: string;
  role?: string;
}

export interface JwtResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private http = inject(HttpClient);

  currentUser = signal<User | null>(null);

  constructor() {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage() {
    const token = this.getToken();
    const username = localStorage.getItem('username');
    const email = localStorage.getItem('email');
    const id = localStorage.getItem('id');
    const role = localStorage.getItem('role');

    if (token && username && email) {
      this.currentUser.set({ 
        id: id ? Number(id) : undefined, 
        username, 
        email,
        role: role || undefined
      });
    }
  }

  login(username: string, password: string): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/signin`, { username, password })
      .pipe(
        tap(response => {
          this.saveAuthData(response);
        })
      );
  }

  register(username: string, email: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/signup`, { username, email, password });
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    localStorage.removeItem('id');
    localStorage.removeItem('role');
    this.currentUser.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getCurrentUser() {
    return this.currentUser();
  }

  isAdmin(): boolean {
    return this.currentUser()?.role === 'ROLE_ADMIN';
  }

  private saveAuthData(response: JwtResponse) {
    localStorage.setItem('token', response.token);
    localStorage.setItem('username', response.username);
    localStorage.setItem('email', response.email);
    localStorage.setItem('role', response.role);
    if (response.id) {
        localStorage.setItem('id', response.id.toString());
    }
    
    this.currentUser.set({
      id: response.id,
      username: response.username,
      email: response.email,
      role: response.role
    });
  }
}