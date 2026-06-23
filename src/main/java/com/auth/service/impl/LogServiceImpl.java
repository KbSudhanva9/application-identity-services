package com.auth.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth.entity.Log;
import com.auth.repository.LogRepository;
import com.auth.service.LogService;

@Service
public class LogServiceImpl implements LogService {

	@Autowired
    private LogRepository repository;

    public void saveLog(Log log) {
        repository.save(log);
    }
	
}
