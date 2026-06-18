package com.auth_service.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.auth_service.service.SchedulerService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SessionExpiryScheduler {

	private final SchedulerService schedulerService;
	
    @Scheduled(fixedRate = 300000)
	public void checkExpiredSessions() {
		schedulerService.expireSessions();
	}
	
}
