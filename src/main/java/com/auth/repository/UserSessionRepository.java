package com.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.auth.entity.UserSession;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Integer>{

	Optional<UserSession> findBySessionIdAndIsSessionExpiredFalse(String sessionId);
	
	@Modifying
    @Query("""
        UPDATE UserSession u
        SET u.isSessionExpired = true
        WHERE u.expireTime <= :now
        AND u.isSessionExpired = false
    """)
    int expireSessions(LocalDateTime now);

	
}
