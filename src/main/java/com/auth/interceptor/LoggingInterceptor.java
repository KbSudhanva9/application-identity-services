package com.auth.interceptor;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.auth.entity.Log;
import com.auth.service.LogService;

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
        System.out.println("logService-data------>"+logService);
        
        Log log = new Log() ;
//        		.userId(request.getRequestId())
//        		.userId(request.getProtocol())
//        		.message(request.getUserPrincipal().toString())
//        		.userId(request.get)
        log.setUserId(request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "Anonymous");
//        log.setReferenceId(request.getHeader("userId") != null ? request.getHeader("userId") : "Anonymous");
//        log.setReferenceId(request.getAttribute("email") != null ? request.getAttribute("email").toString() : "Anonymous");
//        log.setReferenceId(request.getAttribute(""));
        log.setApiEndpoint(request.getRequestURI());
        log.setHttpMethod(request.getMethod());
        log.setIpAddress(request.getRemoteAddr());
        log.setLoggedOn(LocalDateTime.now());
                


        logService.saveLog(log);
        
        return true;
    }

}
