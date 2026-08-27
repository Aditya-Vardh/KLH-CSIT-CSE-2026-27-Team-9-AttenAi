package com.attendai.employee.config;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Seeds demo / development data when {@code SEED_DEMO_DATA=true}.
 *
 * Resolution order for the SQL file:
 *   1. classpath:seed_demo_data.sql  (src/main/resources/seed_demo_data.sql)
 *   2. project-root /seed/seed_demo_data.sql  (relative to working dir)
 *
 * Safe to re-run: all INSERT statements use INSERT IGNORE.
 *
 * NEVER set SEED_DEMO_DATA=true in a production environment.
 */
@Component
@ConditionalOnProperty(name = "SEED_DEMO_DATA", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final JdbcTemplate jdbc;

    public DemoDataSeeder(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public void run(ApplicationArguments args) {
        log.warn("⚠  SEED_DEMO_DATA=true — loading demo data into the database.");
        try {
            String sql = resolveSql();
            // Split on statement delimiters — skip DELIMITER directives
            // (stored procedure block is handled by executing the whole chunk)
            String[] statements = sql.split(";\\s*\\n");
            int executed = 0;
            for (String stmt : statements) {
                String trimmed = stmt.strip();
                if (trimmed.isEmpty()
                        || trimmed.startsWith("--")
                        || trimmed.startsWith("DELIMITER")) continue;
                try {
                    jdbc.execute(trimmed);
                    executed++;
                } catch (Exception e) {
                    // Log and continue — INSERT IGNORE means duplicates are not errors
                    log.debug("Seed statement skipped: {}", e.getMessage());
                }
            }
            log.info("✓  Demo seed complete — {} statements executed.", executed);
        } catch (Exception e) {
            log.error("Demo seed failed — application will continue normally.", e);
        }
    }

    private String resolveSql() throws Exception {
        // Try classpath first
        try {
            ClassPathResource cp = new ClassPathResource("seed_demo_data.sql");
            if (cp.exists()) {
                return new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}

        // Fall back to file system relative to working directory
        Path fs = Paths.get("seed", "seed_demo_data.sql");
        if (Files.exists(fs)) {
            return Files.readString(fs, StandardCharsets.UTF_8);
        }

        throw new IllegalStateException(
            "seed_demo_data.sql not found on classpath or at " + fs.toAbsolutePath());
    }
}
