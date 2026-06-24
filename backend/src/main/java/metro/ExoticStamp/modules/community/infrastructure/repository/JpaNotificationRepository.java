package metro.ExoticStamp.modules.community.infrastructure.repository;

import metro.ExoticStamp.modules.community.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface JpaNotificationRepository extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByUserIdAndId(UUID userId, UUID id);

    @Query("""
            SELECT n FROM Notification n
            WHERE n.userId = :userId
              AND (:unreadOnly = false OR n.read = false)
            ORDER BY n.createdAt DESC
            """)
    Page<Notification> findFiltered(
            @Param("userId") UUID userId,
            @Param("unreadOnly") boolean unreadOnly,
            Pageable pageable
    );

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.read = true, n.readAt = :readAt
            WHERE n.userId = :userId AND n.read = false
            """)
    int markAllReadByUserId(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);
}
