import { Component, computed, input } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { Ticket } from '../../models/ticket.model';

@Component({
  selector: 'app-dashboard-stats',
  standalone: true,
  imports: [BaseChartDirective],
  template: `
    <div class="stats-panel">
      <div class="stats-info">
        <h3>Distribución de Prioridades</h3>
        <p>Visualización en tiempo real de tickets activos.</p>
      </div>
      <div class="chart-container">
        @if (tickets().length === 0) {
          <p class="no-data">Sin datos</p>
        } @else {
          <canvas baseChart
            [data]="doughnutChartData()"
            [options]="doughnutChartOptions"
            [type]="doughnutChartType">
          </canvas>
        }
      </div>
    </div>
  `,
  styles: [`
    .stats-panel {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 2rem;
      padding: 1.5rem 2.5rem;
      background: var(--card-bg);
      border-radius: var(--radius-lg);
      border: 1px solid var(--border-color);
      box-shadow: var(--shadow-sm);
      height: 100%;
    }
    .stats-info h3 { margin: 0; font-size: 1.25rem; color: var(--text-color); }
    .stats-info p { margin: 0.5rem 0 0; color: var(--text-muted); font-size: 0.9rem; }
    .chart-container { position: relative; height: 200px; width: 200px; flex-shrink: 0; }
    .no-data { margin: auto; color: var(--text-muted); font-style: italic; }
    @media (max-width: 600px) {
      .stats-panel { flex-direction: column; text-align: center; padding: 1.5rem; }
    }
  `]
})
export class DashboardStatsComponent {

  // RECIBIMOS los tickets desde el componente padre usando Input Signals
  tickets = input<Ticket[]>([]);

  // Computa los datos para la gráfica automáticamente
  doughnutChartData = computed<ChartData<'doughnut'>>(() => {
    const allTickets = this.tickets();

    const count = { Urgente: 0, Alta: 0, Media: 0, Baja: 0 };

    // Solo contamos tickets que NO estén cerrados
    allTickets
      .filter(t => t.status !== 'CERRADO')
      .forEach(t => {
        if (t.aiPriority === 'Urgente') count.Urgente++;
        else if (t.aiPriority === 'Alta') count.Alta++;
        else if (t.aiPriority === 'Media') count.Media++;
        else if (t.aiPriority === 'Baja') count.Baja++;
      });

    return {
      labels: ['Urgente', 'Alta', 'Media', 'Baja'],
      datasets: [
        {
          data: [count.Urgente, count.Alta, count.Media, count.Baja],
          backgroundColor: ['#ef4444', '#f97316', '#eab308', '#3b82f6'],
          hoverBackgroundColor: ['#dc2626', '#ea580c', '#ca8a04', '#2563eb']
        }
      ]
    };
  });

  public doughnutChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom' }
    }
  };

  public doughnutChartType: ChartType = 'doughnut';
}