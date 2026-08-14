package com.jobintel.match;

import com.jobintel.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    List<MatchResult> findByUserOrderByCreatedAtDesc(User user);
    Optional<MatchResult> findByIdAndUser(Long id, User user);
}
