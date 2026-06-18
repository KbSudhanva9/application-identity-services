package com.auth_service.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.auth_service.repository.UserSessionRepository;
import com.auth_service.service.SchedulerService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SchedulerServiceImpl implements SchedulerService {
	
	private final UserSessionRepository userSessionRepository;

	@Override
    @Transactional
    public void expireSessions() {

        int updated = userSessionRepository.expireSessions(LocalDateTime.now());

        System.out.println(updated + " sessions expired");
    }

}
