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
    <div class="reports-container">
      <header class="reports-header">
        <h1>📈 Dashboard de Analítica</h1>
        <p>Métricas de rendimiento e insights generados por IA en tiempo real.</p>
      </header>

      <div class="stats-grid-top">
        <div class="stat-card primary">
          <div class="stat-icon">🤖</div>
          <div class="stat-info">
            <span class="stat-label">Precisión IA</span>
            <h2 class="stat-value">98.5%</h2>
          </div>
        </div>
        <div class="stat-card success">
          <div class="stat-icon">✅</div>
          <div class="stat-info">
            <span class="stat-label">Tickets Resueltos</span>
            <h2 class="stat-value">{{ totalResolved() }}</h2>
          </div>
        </div>
        <div class="stat-card warning">
          <div class="stat-icon">⏳</div>
          <div class="stat-info">
            <span class="stat-label">Tiempo Medio</span>
            <h2 class="stat-value">1.4h</h2>
          </div>
        </div>
      </div>

      <div class="charts-container">
        <!-- GRÁFICO DE CATEGORÍAS (BARRAS) -->
        <div class="chart-wrapper">
          <h3>Distribución por Categorías</h3>
          <div class="canvas-container">
            <canvas baseChart
              [data]="categoryChartData"
              [options]="barChartOptions"
              [type]="'bar'">
            </canvas>
          </div>
        </div>

        <!-- GRÁFICO DE PRIORIDADES (PIE) -->
        <div class="chart-wrapper">
          <h3>Nivel de Urgencia (IA)</h3>
          <div class="canvas-container">
            <canvas baseChart
              [data]="priorityChartData"
              [options]="pieChartOptions"
              [type]="'pie'">
            </canvas>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .reports-container {
      padding: 2.5rem;
      max-width: 1200px;
      margin: 0 auto;
    }
    .reports-header {
      margin-bottom: 2.5rem;
    }
    .reports-header h1 {
      font-size: 2.25rem;
      margin-bottom: 0.5rem;
      color: #111827;
    }
    .reports-header p {
      color: #6b7280;
      font-size: 1.1rem;
    }
    .stats-grid-top {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 1.5rem;
      margin-bottom: 2.5rem;
    }
    .stat-card {
      background: white;
      padding: 1.5rem;
      border-radius: 16px;
      display: flex;
      align-items: center;
      gap: 1.25rem;
      box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
      border: 1px solid #f3f4f6;
    }
    .stat-icon {
      font-size: 2.5rem;
      background: #f9fafb;
      width: 60px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12px;
    }
    .stat-label {
      color: #6b7280;
      font-size: 0.875rem;
      font-weight: 500;
    }
    .stat-value {
      font-size: 1.5rem;
      margin: 0;
      color: #111827;
    }
    .charts-container {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
      gap: 2rem;
    }
    .chart-wrapper {
      background: white;
      padding: 2rem;
      border-radius: 20px;
      box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1);
      border: 1px solid #f3f4f6;
    }
    .chart-wrapper h3 {
      text-align: center;
      margin-bottom: 2rem;
      color: #374151;
      font-size: 1.25rem;
    }
    .canvas-container {
      height: 350px;
      position: relative;
    }
    @media (max-width: 640px) {
      .charts-container { grid-template-columns: 1fr; }
      .canvas-container { height: 300px; }
    }
  `]
})
export class Reports implements OnInit {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/tickets`;

  totalResolved = signal<number>(0);

  // CONFIG BARRAS (Categorías)
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false }
    }
  };

  public categoryChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [{ 
      data: [], 
      label: 'Tickets',
      backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'],
      borderRadius: 10
    }]
  };

  // CONFIG PIE (Prioridades)
  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom' }
    }
  };

  public priorityChartData: ChartData<'pie'> = {
    labels: [],
    datasets: [{ 
      data: [],
      backgroundColor: ['#ef4444', '#f59e0b', '#3b82f6', '#10b981'] 
    }]
  };

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    // 1. Categorías
    this.http.get<Record<string, number>>(`${this.apiUrl}/stats/categories`).subscribe(stats => {
      this.categoryChartData = {
        labels: Object.keys(stats),
        datasets: [{ 
          ...this.categoryChartData.datasets[0], 
          data: Object.values(stats) 
        }]
      };
    });

    // 2. Prioridades
    this.http.get<Record<string, number>>(`${this.apiUrl}/stats/priorities`).subscribe(stats => {
      this.priorityChartData = {
        labels: Object.keys(stats),
        datasets: [{ 
          ...this.priorityChartData.datasets[0], 
          data: Object.values(stats) 
        }]
      };
    });

    // 3. Resueltos
    this.http.get<any>(`${this.apiUrl}?size=1000`).subscribe(res => {
      const tickets = res.content || [];
      const resolved = tickets.filter((t: any) => t.status === 'RESUELTO' || t.status === 'CERRADO').length;
      this.totalResolved.set(resolved);
    });
  }
}
