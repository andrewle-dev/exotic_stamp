package metro.ExoticStamp.config;

import metro.ExoticStamp.ExoticStampApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtSecretRequiredTest {

    private static final String AUTOCONFIG_EXCLUDE =
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration";

    @Test
    void failsWhenJwtSecretMissing_underDevProfile() {
        assertThatThrownBy(() -> runDevWithJwtSecret(""))
                .isInstanceOf(Exception.class);
    }

    @Test
    void failsWhenJwtSecretPropertyAbsent_underDevProfile() {
        assertThatThrownBy(() -> {
            var app = new SpringApplication(ExoticStampApplication.class);
            app.setWebApplicationType(WebApplicationType.NONE);
            app.run(
                    "--spring.profiles.active=dev",
                    "--spring.datasource.url=jdbc:postgresql://localhost:5432/unused",
                    "--spring.datasource.username=postgres",
                    "--spring.datasource.password=postgres",
                    "--spring.data.redis.host=localhost",
                    "--spring.mail.username=dev@example.com",
                    "--spring.mail.password=dev-mail-password",
                    "--application.mail.from=dev@example.com",
                    "--application.bootstrap.admin-password=test-admin",
                    "--application.bootstrap.demo-user-password=test-demo",
                    "--spring.flyway.enabled=false",
                    "--spring.jpa.hibernate.ddl-auto=none",
                    "--spring.autoconfigure.exclude=" + AUTOCONFIG_EXCLUDE
                    // jwt.secret intentionally omitted; application-dev requires ${JWT_SECRET}
            );
        }).isInstanceOf(Exception.class);
    }

    private static void runDevWithJwtSecret(String secret) {
        var app = new SpringApplication(ExoticStampApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(
                "--spring.profiles.active=dev",
                "--jwt.secret=" + secret,
                "--spring.datasource.url=jdbc:postgresql://localhost:5432/unused",
                "--spring.datasource.username=postgres",
                "--spring.datasource.password=postgres",
                "--spring.data.redis.host=localhost",
                "--spring.mail.username=dev@example.com",
                "--spring.mail.password=dev-mail-password",
                "--application.mail.from=dev@example.com",
                "--application.bootstrap.admin-password=test-admin",
                "--application.bootstrap.demo-user-password=test-demo",
                "--spring.flyway.enabled=false",
                "--spring.jpa.hibernate.ddl-auto=none",
                "--spring.autoconfigure.exclude=" + AUTOCONFIG_EXCLUDE
        );
    }
}
