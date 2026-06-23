package com.auth_service.interceptor;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.auth_service.entity.Log;
import com.auth_service.service.LogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
	
	@Autowired
	private LogService logService;
	
	@Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        System.out.println("Interceptor Hit : " + request.getRequestURI());
//        System.out.println("logService-data------>"+logService);
        
        Log log = Log.builder()
//        		.userId(request.getRequestId())
//        		.userId(request.getProtocol())
//        		.message(request.getUserPrincipal().toString())
//        		.userId(request.get)
                .apiEndpoint(request.getRequestURI())
                .httpMethod(request.getMethod())
                .ipAddress(request.getRemoteAddr())
                .loggedOn(LocalDateTime.now())
                .build();


        logService.saveLog(log);
        
        return true;
    }

}
