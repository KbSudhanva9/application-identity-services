package com.auth.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth.repository.UserSessionRepository;
import com.auth.service.SchedulerService;

import jakarta.transaction.Transactional;


@Service

public class SchedulerServiceImpl implements SchedulerService {
	
	@Autowired
	private   UserSessionRepository userSessionRepository;

	@Override
    @Transactional
    public void expireSessions() {

        int updated = userSessionRepository.expireSessions(LocalDateTime.now());

        System.out.println(updated + " sessions expired");
    }

}
