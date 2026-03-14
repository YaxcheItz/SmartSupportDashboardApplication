import { Injectable, signal, effect } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {

  // Signal para guardar el estado del tema
  // true = oscuro | false = claro
  isDarkMode = signal<boolean>(false);

  constructor() {

    // 1️⃣ Revisar si el usuario ya tenía un tema guardado
    const savedTheme = localStorage.getItem('theme');

    if (savedTheme === 'dark') {
      this.isDarkMode.set(true);
      document.body.classList.add('dark-theme');
    }

    // 2️⃣ effect: se ejecuta cada vez que cambia el signal
    effect(() => {

      if (this.isDarkMode()) {

        document.body.classList.add('dark-theme');
        localStorage.setItem('theme', 'dark');

      } else {

        document.body.classList.remove('dark-theme');
        localStorage.setItem('theme', 'light');

      }

    });

  }

  // 3️⃣ Método para cambiar el tema
  toggleTheme() {
    this.isDarkMode.update(current => !current);
  }

}