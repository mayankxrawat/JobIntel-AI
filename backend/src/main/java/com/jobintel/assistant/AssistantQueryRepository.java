package com.jobintel.assistant;

import com.jobintel.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssistantQueryRepository extends JpaRepository<AssistantQuery, Long> {
    List<AssistantQuery> findByUserOrderByCreatedAtDesc(User user);
}
