package com.jobintel.application;

import com.jobintel.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUserOrderByAppliedDateDesc(User user);
    Optional<Application> findByIdAndUser(Long id, User user);
    long countByUserAndStatus(User user, ApplicationStatus status);
    long countByUser(User user);
}
