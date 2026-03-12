package com.yaxcherg.smartsupportdashboard.repository;

import com.yaxcherg.smartsupportdashboard.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // JpaRepository ya incluye métodos como save(), findAll(), findById(), deleteById(), etc.
    // ¡No tienes que programar el código SQL para hacer un CRUD básico!
}