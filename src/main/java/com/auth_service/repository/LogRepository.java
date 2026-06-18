package com.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.auth_service.entity.Log;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {

}
