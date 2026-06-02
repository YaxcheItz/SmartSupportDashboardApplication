package com.yaxcherg.smartsupportdashboard;

import com.yaxcherg.smartsupportdashboard.model.AppUser;
import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.model.enums.TicketStatus;
import com.yaxcherg.smartsupportdashboard.model.enums.UserRole;
import com.yaxcherg.smartsupportdashboard.repository.TicketRepository;
import com.yaxcherg.smartsupportdashboard.repository.UserRepository;
import com.yaxcherg.smartsupportdashboard.repository.FaqRepository;
import com.yaxcherg.smartsupportdashboard.model.Faq;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCaching
public class SmartSupportDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartSupportDashboardApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(TicketRepository ticketRepository, 
                                    UserRepository userRepository, 
                                    FaqRepository faqRepository,
                                    PasswordEncoder passwordEncoder) {
        return (args) -> {
            // Sembrar FAQs si no hay ninguna
            if (faqRepository.count() == 0) {
                faqRepository.save(new Faq(
                    "¿Cómo cambio mi contraseña?", 
                    "Ve a tu perfil, selecciona 'Editar' y completa el campo de nueva contraseña."
                ));
                faqRepository.save(new Faq(
                    "Problema con el login / Error 400", 
                    "Asegúrate de que tu contraseña tenga al menos 8 caracteres y que el usuario sea el correcto. Limpia la caché de tu navegador si el problema persiste."
                ));
                faqRepository.save(new Faq(
                    "¿Qué formatos de imagen se aceptan?", 
                    "Aceptamos JPG, PNG y GIF con un tamaño máximo de 5MB por archivo."
                ));
                System.out.println("✅ Se han insertado 3 FAQs de prueba.");
            }

            // Sembrar Usuarios de Prueba si no hay ninguno
            if (userRepository.count() == 0) {
                // Admin
                AppUser admin = new AppUser("admin", "admin@gmail.com", passwordEncoder.encode("admin123"));
                admin.setRole(UserRole.ROLE_ADMIN);
                userRepository.save(admin);

                // Agente
                AppUser agent = new AppUser("agente", "agente@gmail.com", passwordEncoder.encode("agente123"));
                agent.setRole(UserRole.ROLE_EMPLOYEE);
                userRepository.save(agent);

                // Cliente/Usuario común
                AppUser user = new AppUser("cliente", "cliente@gmail.com", passwordEncoder.encode("cliente123"));
                user.setRole(UserRole.ROLE_USER);
                userRepository.save(user);

                System.out.println("✅ Se han insertado 3 usuarios de prueba (admin, agente, cliente).");
            }

            // Si la base de datos está vacía, mete dos tickets de prueba
            if (ticketRepository.count() == 0) {
                AppUser testUser = userRepository.findByUsername("cliente").orElse(null);

                Ticket t1 = new Ticket();
                t1.setTitle("Problema de conexión");
                t1.setDescription("No puedo entrar a mi cuenta desde ayer.");
                t1.setCustomerEmail("cliente@gmail.com");
                t1.setCreatedBy(testUser); 
                t1.setStatus(TicketStatus.ABIERTO);
                t1.setAiCategory("Soporte Técnico");
                t1.setAiPriority("Alta");
                t1.setAiTone("Frustrado");
                t1.setAiSummary("Usuario no puede iniciar sesión");
                ticketRepository.save(t1);

                Ticket t2 = new Ticket();
                t2.setTitle("Duda sobre mi factura");
                t2.setDescription("¿Por qué me cobraron doble este mes?");
                t2.setCustomerEmail("cliente@gmail.com");
                t2.setCreatedBy(testUser); 
                t2.setStatus(TicketStatus.ABIERTO);
                t2.setAiCategory("Facturación");
                t2.setAiPriority("Media");
                t2.setAiTone("Preocupado");
                t2.setAiSummary("Consulta sobre cobro duplicado");
                ticketRepository.save(t2);

                System.out.println("✅ Se han insertado 2 tickets de prueba relacionados al usuario cliente.");
            }
        };
    }
}
