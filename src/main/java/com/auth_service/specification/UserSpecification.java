package com.auth_service.specification;

import com.auth_service.dto.UserFilterRequest;
import com.auth_service.entity.User;

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
            if (request.getName() != null &&!request.getName().trim().isEmpty()) {
                predicates.add(
                        cb.like(cb.lower(root.get("name")), "%" + request.getName().toLowerCase() + "%"));
            }

            // EMAIL
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                predicates.add(
                        cb.like(cb.lower(root.get("email")), "%" + request.getEmail().toLowerCase() + "%"));
            }

            // ROLE
            if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("role"), request.getRole()));
            }

//            // USED DEPOSIT
//            if (request.getUsedDeposit() != null) {
//                predicates.add(
//                        cb.equal(root.get("usedDeposit"), request.getUsedDeposit()));
//            }
//
//            // DEPOSIT
//            if (request.getDeposit() != null) {
//                predicates.add(
//                        cb.equal(root.get("deposit"), request.getDeposit()));
//            }

            // IS ACTIVE
            if (request.getIsActive() != null) {
                predicates.add(
                        cb.equal(root.get("isActive"), request.getIsActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}