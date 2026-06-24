package metro.ExoticStamp.modules.reward.infrastructure.repository;

import metro.ExoticStamp.modules.reward.domain.model.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaPartnerRepository extends JpaRepository<Partner, UUID> {

    Page<Partner> findByActive(boolean active, Pageable pageable);
}
