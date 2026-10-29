
package com.clickstore.clickstore;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DbTestController {

    private final JdbcTemplate jdbcTemplate;

    public DbTestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/db-test")
    public Map<String, Object> testDatabase() {
        return jdbcTemplate.queryForMap(
                "SELECT current_database() AS database_name, " +
                        "COUNT(*) AS table_count " +
                        "FROM information_schema.tables " +
                        "WHERE table_schema = 'public' " +
                        "AND table_type = 'BASE TABLE'"
        );
    }
}
