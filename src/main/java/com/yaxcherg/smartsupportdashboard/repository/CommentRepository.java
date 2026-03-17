package com.yaxcherg.smartsupportdashboard.repository;

import com.yaxcherg.smartsupportdashboard.model.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<TicketComment, Long> {
    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
