package com.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.auth.entity.MasterRedirections;

@Repository
public interface MasterRedirectionsRepository extends JpaRepository<MasterRedirections, Integer> {
	
	MasterRedirections findByRole(String role);

}
