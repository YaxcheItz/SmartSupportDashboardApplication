package com.yaxcherg.smartsupportdashboard;

import com.yaxcherg.smartsupportdashboard.model.AppUser;
import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.repository.TicketRepository;
import com.yaxcherg.smartsupportdashboard.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableAsync
public class SmartSupportDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartSupportDashboardApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(TicketRepository ticketRepository, 
                                    UserRepository userRepository, 
                                    PasswordEncoder passwordEncoder) {
        return (args) -> {
            // Sembrar Usuarios de Prueba si no hay ninguno
            if (userRepository.count() == 0) {
                // Admin
                AppUser admin = new AppUser("admin", "admin@smart.com", passwordEncoder.encode("admin123"));
                admin.setRole("ROLE_ADMIN");
                userRepository.save(admin);

                // Agente
                AppUser agent = new AppUser("agente", "agente@smart.com", passwordEncoder.encode("agente123"));
                agent.setRole("ROLE_EMPLOYEE");
                userRepository.save(agent);

                // Cliente/Usuario común
                AppUser user = new AppUser("cliente", "cliente@smart.com", passwordEncoder.encode("cliente123"));
                user.setRole("ROLE_USER");
                userRepository.save(user);

                System.out.println("✅ Se han insertado 3 usuarios de prueba (admin, agente, cliente).");

            }

            // Si la base de datos está vacía, mete dos tickets de prueba
            if (ticketRepository.count() == 0) {
                // ... (existing code for tickets)

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