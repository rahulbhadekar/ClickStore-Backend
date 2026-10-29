
package com.clickstore.clickstore.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Assign CUSTOMER role
    public void assignCustomerRole(Long userId) {
        jdbcTemplate.update(
                """
                INSERT INTO user_roles (user_id, role_id, assigned_at)
                SELECT ?, 1, CURRENT_TIMESTAMP
                WHERE NOT EXISTS (
                    SELECT 1 FROM user_roles
                    WHERE user_id = ? AND role_id = 1
                )
                """,
                userId,
                userId
        );
    }

    // Assign SELLER role after admin approval
    public void assignSellerRole(Long userId) {
        jdbcTemplate.update(
                """
                INSERT INTO user_roles (user_id, role_id, assigned_at)
                SELECT ?, 2, CURRENT_TIMESTAMP
                WHERE NOT EXISTS (
                    SELECT 1 FROM user_roles
                    WHERE user_id = ? AND role_id = 2
                )
                """,
                userId,
                userId
        );
    }

    // Find all roles for the logged-in user
    public List<String> findRoleNamesByUserId(Long userId) {
        return jdbcTemplate.queryForList(
                """
                SELECT r.role_name
                FROM roles r
                JOIN user_roles ur ON ur.role_id = r.id
                WHERE ur.user_id = ?
                """,
                String.class,
                userId
        );
    }


    // Find IDs of users who have the CUSTOMER role
    public List<Long> findCustomerUserIds() {
        return jdbcTemplate.queryForList(
                """
                SELECT DISTINCT ur.user_id
                FROM user_roles ur
                JOIN roles r ON r.id = ur.role_id
                WHERE r.role_name = 'CUSTOMER'
                ORDER BY ur.user_id
                """,
                Long.class
        );
    }


}
