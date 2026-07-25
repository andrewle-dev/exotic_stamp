package metro.ExoticStamp.config;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CI-only Docker availability gate for Failsafe.
 * Local runs without {@code -Pci} skip when Docker/Testcontainers is absent.
 * CI profile sets {@code ci.require-docker=true} and must fail when unavailable.
 * Detection uses Testcontainers' client factory (same path as ITs).
 */
class CiDockerAvailabilityIT {

    @Test
    void dockerAvailableWhenRequiredByCi() {
        boolean requireDocker = Boolean.parseBoolean(
                System.getProperty("ci.require-docker", "false"));
        boolean available;
        try {
            available = DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            available = false;
        }

        if (requireDocker) {
            assertTrue(
                    available,
                    "CI requires Docker for Testcontainers integration tests "
                            + "(ci.require-docker=true). Start Docker Desktop/engine and re-run: "
                            + "mvn verify -Pci");
            return;
        }

        Assumptions.assumeTrue(
                available,
                "Docker/Testcontainers not available locally; CiDockerAvailabilityIT skipped outside -Pci");
    }
}
