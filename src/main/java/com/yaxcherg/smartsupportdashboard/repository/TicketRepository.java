package com.yaxcherg.smartsupportdashboard.repository;

import com.yaxcherg.smartsupportdashboard.model.AppUser;
import com.yaxcherg.smartsupportdashboard.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    Page<Ticket> findByCreatedBy(AppUser createdBy, Pageable pageable);

    @Query("SELECT t.aiCategory, COUNT(t) FROM Ticket t WHERE t.aiCategory IS NOT NULL GROUP BY t.aiCategory")
    List<Object[]> countTicketsByCategory();
}