package metro.ExoticStamp.modules.reward.infrastructure.repository;

import metro.ExoticStamp.modules.reward.domain.model.Reward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface JpaRewardRepository extends JpaRepository<Reward, UUID> {

    Optional<Reward> findByMilestoneIdAndActiveTrue(UUID milestoneId);

    Page<Reward> findByActive(boolean active, Pageable pageable);

    List<Reward> findByIdIn(Iterable<UUID> rewardIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE rewards SET issued_count = issued_count + 1
            WHERE id = :id AND is_active = TRUE
            AND (total_stock IS NULL OR issued_count < total_stock)
            """, nativeQuery = true)
    int incrementIssuedCountIfStockAllows(@Param("id") UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE rewards SET issued_count = issued_count - 1
            WHERE id = :id AND issued_count > 0
            """, nativeQuery = true)
    int decrementIssuedCount(@Param("id") UUID id);
}
