package com.auth.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.auth.service.SchedulerService;

 

@Component
 
public class SessionExpiryScheduler {

	@Autowired
	private   SchedulerService schedulerService;
	
    @Scheduled(fixedRate = 300000)
	public void checkExpiredSessions() {
		schedulerService.expireSessions();
	}
	
}
