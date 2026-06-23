package com.auth.specification;

import com.auth.dto.UserFilterRequest;
import com.auth.entity.User;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> filterUsers(
            UserFilterRequest request
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // NAME
            if (request.name() != null &&!request.name().trim().isEmpty()) {
                predicates.add(
                        cb.like(cb.lower(root.get("name")), "%" + request.name().toLowerCase() + "%"));
            }

            // EMAIL
            if (request.email() != null && !request.email().trim().isEmpty()) {
                predicates.add(
                        cb.like(cb.lower(root.get("email")), "%" + request.email().toLowerCase() + "%"));
            }

            // ROLE
            if (request.role() != null && !request.role().trim().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("role"), request.role()));
            }

            // IS ACTIVE
            if (request.isActive() != null) {
                predicates.add(
                        cb.equal(root.get("isActive"), request.isActive()));
            }
            
            // IS ACTIVE
            if (request.phone() != null && !request.phone().trim().isEmpty()) {
                predicates.add(
                        cb.like(root.get("phone"),  "%" + request.phone() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}