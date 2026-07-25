package metro.ExoticStamp.modules.reward.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.reward.application.port.RewardReconcileLockPort;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Session-scoped PostgreSQL advisory lock held on a dedicated borrowed connection
 * for the duration of a reconcile run. Unlock + connection release on {@link #release()}.
 * A crashed JVM releases the lock when the TCP session drops — no permanent deadlock.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RewardReconcileAdvisoryLockAdapter implements RewardReconcileLockPort {

    /** Stable int8 key for reward reconcile exclusivity. */
    static final long LOCK_KEY = 0x4553525245434F4EL; // ESRRECON

    private final DataSource dataSource;

    private final ThreadLocal<Connection> heldConnection = new ThreadLocal<>();

    @Override
    public boolean tryAcquire() {
        if (heldConnection.get() != null) {
            return true;
        }
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            try (PreparedStatement ps = connection.prepareStatement("SELECT pg_try_advisory_lock(?)")) {
                ps.setLong(1, LOCK_KEY);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean ok = rs.next() && rs.getBoolean(1);
                    if (!ok) {
                        DataSourceUtils.releaseConnection(connection, dataSource);
                        return false;
                    }
                    heldConnection.set(connection);
                    return true;
                }
            }
        } catch (Exception e) {
            DataSourceUtils.releaseConnection(connection, dataSource);
            log.warn("[RewardReconcile] advisory lock acquire failed: {}", e.getClass().getSimpleName());
            return false;
        }
    }

    @Override
    public void release() {
        Connection connection = heldConnection.get();
        if (connection == null) {
            return;
        }
        try {
            try (PreparedStatement ps = connection.prepareStatement("SELECT pg_advisory_unlock(?)")) {
                ps.setLong(1, LOCK_KEY);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                }
            }
        } catch (Exception e) {
            log.warn("[RewardReconcile] advisory unlock failed: {}", e.getClass().getSimpleName());
        } finally {
            heldConnection.remove();
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
