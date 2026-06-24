package metro.ExoticStamp.modules.community.infrastructure.repository;

import metro.ExoticStamp.modules.community.domain.model.ShareEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaShareEventRepository extends JpaRepository<ShareEvent, UUID> {

    Page<ShareEvent> findByUserIdOrderBySharedAtDesc(UUID userId, Pageable pageable);
}
