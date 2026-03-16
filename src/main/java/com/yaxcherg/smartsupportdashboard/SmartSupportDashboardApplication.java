package com.yaxcherg.smartsupportdashboard;

import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.repository.TicketRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SmartSupportDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartSupportDashboardApplication.class, args);
    }

    // ¡Añade este método!
    // Esto se ejecuta automáticamente cuando Java arranca
    @Bean
    public CommandLineRunner loadData(TicketRepository ticketRepository) {
        return (args) -> {
            // Si la base de datos está vacía, mete dos tickets de prueba
            if (ticketRepository.count() == 0) {
                Ticket t1 = new Ticket();
                t1.setTitle("Problema de conexión");
                t1.setDescription("No puedo entrar a mi cuenta desde ayer.");
                t1.setCustomerEmail("juan@ejemplo.com");
                t1.setAiCategory("Soporte Técnico");
                t1.setAiPriority("Alta");
                t1.setAiTone("Frustrado");
                t1.setAiSummary("Usuario no puede iniciar sesión");
                ticketRepository.save(t1);

                Ticket t2 = new Ticket();
                t2.setTitle("Duda sobre mi factura");
                t2.setDescription("¿Por qué me cobraron doble este mes?");
                t2.setCustomerEmail("maria@ejemplo.com");
                t2.setAiCategory("Facturación");
                t2.setAiPriority("Media");
                t2.setAiTone("Preocupado");
                t2.setAiSummary("Consulta sobre cobro duplicado");
                ticketRepository.save(t2);

                System.out.println("✅ Se han insertado 2 tickets de prueba en H2.");
            }
        };
    }
}