package metro.ExoticStamp.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

import metro.ExoticStamp.ExoticStampApplication;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtSecretRequiredTest {

    @Test
    void failsWhenJwtSecretMissing() {
        assertThatThrownBy(() -> {
            var app = new SpringApplication(ExoticStampApplication.class);
            app.setWebApplicationType(WebApplicationType.NONE);
            app.run(
                    "--spring.profiles.active=dev",
                    "--jwt.secret=",
                    "--spring.datasource.url=jdbc:postgresql://localhost:5432/unused",
                    "--spring.datasource.username=postgres",
                    "--spring.datasource.password=postgres",
                    "--spring.data.redis.host=localhost",
                    "--spring.data.redis.port=6379",
                    "--spring.flyway.enabled=false",
                    "--spring.jpa.hibernate.ddl-auto=none",
                    "--spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                            + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
            );
        }).isInstanceOf(Exception.class);
    }
}
