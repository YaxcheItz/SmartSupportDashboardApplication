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
        <h3>Métricas de Prioridad</h3>
      </div>
      <div class="chart-container">
        @if (tickets().length === 0) {
          <p class="no-data">Sin tickets activos</p>
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
      flex-direction: column;
      align-items: center;
      gap: 1.5rem;
      padding: 1.5rem 1rem;
      background: var(--bg-color);
      border-radius: var(--radius-md);
      border: 1px solid var(--border-color);
      width: 100%;
      box-sizing: border-box;
    }
    .stats-info h3 { 
      margin: 0; 
      font-size: 0.8rem; 
      text-transform: uppercase; 
      letter-spacing: 0.05em; 
      color: var(--text-muted); 
      font-weight: 700; 
      text-align: center;
    }
    .chart-container { 
      position: relative; 
      height: 160px; /* Tamaño perfecto para la barra lateral */
      width: 100%; 
      display: flex; 
      justify-content: center; 
    }
    .no-data { 
      margin: auto; 
      color: var(--text-muted); 
      font-style: italic; 
      font-size: 0.85rem; 
    }
  `]
})
export class DashboardStatsComponent {
  tickets = input<Ticket[]>([]);

  doughnutChartData = computed<ChartData<'doughnut'>>(() => {
    const allTickets = this.tickets();
    const count = { Urgente: 0, Alta: 0, Media: 0, Baja: 0 };

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
          hoverBackgroundColor: ['#dc2626', '#ea580c', '#ca8a04', '#2563eb'],
          borderWidth: 0 /* Hace que la gráfica se vea más moderna */
        }
      ]
    };
  });

  public doughnutChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { 
        position: 'bottom',
        labels: {
          padding: 15,
          usePointStyle: true,
          pointStyle: 'circle',
          font: { size: 11 }
        }
      }
    }
  };

  public doughnutChartType: ChartType = 'doughnut';
}