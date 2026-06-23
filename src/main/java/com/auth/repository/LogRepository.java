package com.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.auth.entity.Log;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {

}
