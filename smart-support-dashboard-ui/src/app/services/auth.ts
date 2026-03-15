import { Injectable, signal } from '@angular/core';
import { createClient, SupabaseClient, User } from '@supabase/supabase-js';
import { from, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // ⚠️ REEMPLAZA ESTOS VALORES CON LOS DE TU PANEL DE SUPABASE (Settings -> API)
  private supabaseUrl = 'https://iiuqvfoqpuelljtawnxh.supabase.co';
  private supabaseKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlpdXF2Zm9xcHVlbGxqdGF3bnhoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzM1MzI0NjMsImV4cCI6MjA4OTEwODQ2M30.Om9Jm1w5BVm_4L_wiK7-MdyMSQtW1mLa6PN0N_-CPIY';

  private supabase: SupabaseClient;

  // Signal reactivo para saber en toda la app si el usuario está logueado
  currentUser = signal<User | null>(null);

  constructor() {
    this.supabase = createClient(this.supabaseUrl, this.supabaseKey);

    // Al iniciar el servicio, verificamos si ya hay una sesión guardada en el navegador
    this.supabase.auth.getSession().then(({ data }) => {
      if (data.session) {
        this.currentUser.set(data.session.user);
      }
    });

    // Escuchamos cambios (cuando hace login o logout)
    this.supabase.auth.onAuthStateChange((event, session) => {
      if (event === 'SIGNED_IN' || event === 'TOKEN_REFRESHED') {
        this.currentUser.set(session?.user || null);
      } else if (event === 'SIGNED_OUT') {
        this.currentUser.set(null);
      }
    });
  }

  // Método para iniciar sesión
  signIn(email: string, password: string): Observable<any> {
    const promise = this.supabase.auth.signInWithPassword({ email, password });
    return from(promise); // Convertimos la Promesa a Observable para seguir el estilo de Angular
  }

  // Método para cerrar sesión
  async signOut() {
    await this.supabase.auth.signOut();
  }

  // Método auxiliar para saber si está logueado
  isLoggedIn(): boolean {
    return this.currentUser() !== null;
  }
}