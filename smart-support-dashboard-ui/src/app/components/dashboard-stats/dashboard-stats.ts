import { Component, computed, input } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { Ticket } from '../../models/ticket.model';

@Component({
  selector: 'app-dashboard-stats',
  standalone: true,
  imports: [BaseChartDirective],
  template: `
    <div class="stats-container" style="background: var(--card-bg); padding: 1.5rem; border-radius: var(--radius-lg); border: 1px solid var(--border-color); text-align: center; height: 100%; display: flex; flex-direction: column;">
      <h3 style="margin-top: 0; margin-bottom: 1.5rem; color: var(--text-color); font-size: 1.125rem;">Distribución por Prioridad IA</h3>

      <!-- Si no hay tickets, mostramos un mensaje, si hay, mostramos la gráfica -->
      @if (tickets().length === 0) {
        <p style="color: var(--text-muted); margin: auto;">No hay datos para la gráfica.</p>
      } @else {
        <div style="display: block; margin: auto; height: 180px; width: 100%; max-width: 250px;">
          <canvas baseChart
            [data]="doughnutChartData()"
            [options]="doughnutChartOptions"
            [type]="doughnutChartType">
          </canvas>
        </div>
      }
    </div>
  `
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