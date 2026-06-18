package com.auth_service.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth_service.entity.Log;
import com.auth_service.repository.LogRepository;
import com.auth_service.service.LogService;

@Service
public class LogServiceImpl implements LogService {

	@Autowired
    private LogRepository repository;

    public void saveLog(Log log) {
        repository.save(log);
    }
	
}
