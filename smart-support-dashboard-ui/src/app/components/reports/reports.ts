import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  template: `
    <div class="reports-container" style="padding: 2rem;">
      <header style="margin-bottom: 2rem;">
        <h1 style="color: var(--text-color);">📈 Reportes y Analítica</h1>
        <p style="color: var(--text-muted);">Visualiza el rendimiento y categorización del soporte técnico.</p>
      </header>

      <div class="charts-grid" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(400px, 1fr)); gap: 2rem;">
        
        <!-- GRÁFICO DE CATEGORÍAS -->
        <div class="chart-card" style="background: var(--card-bg); padding: 1.5rem; border-radius: var(--radius-lg); border: 1px solid var(--border-color); box-shadow: var(--shadow-md);">
          <h3 style="margin-bottom: 1.5rem; text-align: center; color: var(--text-color);">Distribución por Categorías (IA)</h3>
          <div style="height: 300px; display: flex; justify-content: center;">
            <canvas baseChart
              [data]="barChartData"
              [options]="barChartOptions"
              [type]="'bar'">
            </canvas>
          </div>
        </div>

        <!-- KPI CARDS -->
        <div class="stats-overview" style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 1rem;">
          <div class="stat-box" style="background: var(--primary-color); color: white; padding: 1.5rem; border-radius: var(--radius-lg); text-align: center;">
            <span style="font-size: 2rem; display: block; margin-bottom: 0.5rem;">🤖</span>
            <small>Eficiencia IA</small>
            <h2 style="margin: 0;">98.5%</h2>
          </div>
          <div class="stat-box" style="background: var(--success-color); color: white; padding: 1.5rem; border-radius: var(--radius-lg); text-align: center;">
            <span style="font-size: 2rem; display: block; margin-bottom: 0.5rem;">✅</span>
            <small>Tickets Resueltos</small>
            <h2 style="margin: 0;">{{ totalResolved() }}</h2>
          </div>
        </div>

      </div>
    </div>
  `,
  styles: [`
    :host { display: block; background: var(--bg-color); min-height: 100vh; }
  `]
})
export class Reports implements OnInit {
  private http = inject(HttpClient);
  private apiUrl = window.location.hostname === 'localhost' 
    ? 'http://localhost:8080/api/tickets'
    : 'https://smart-support-dashboard.onrender.com/api/tickets';

  totalResolved = signal<number>(0);

  // Configuración del gráfico de barras
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    scales: {
      x: {},
      y: { min: 0 }
    },
    plugins: {
      legend: { display: false }
    }
  };

  public barChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [
      { 
        data: [], 
        label: 'Cantidad de Tickets',
        backgroundColor: [
          '#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#6366f1'
        ],
        borderRadius: 8
      }
    ]
  };

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    // 1. Cargar estadísticas de categorías
    this.http.get<Record<string, number>>(`${this.apiUrl}/stats/categories`).subscribe(stats => {
      const labels = Object.keys(stats);
      const values = Object.values(stats);
      
      this.barChartData = {
        labels: labels,
        datasets: [{ ...this.barChartData.datasets[0], data: values }]
      };
    });

    // 2. Cargar total resueltos (simplificado para el ejemplo)
    this.http.get<any>(`${this.apiUrl}?size=100`).subscribe(res => {
      const resolved = res.content.filter((t: any) => t.status === 'CERRADO').length;
      this.totalResolved.set(resolved);
    });
  }
}
