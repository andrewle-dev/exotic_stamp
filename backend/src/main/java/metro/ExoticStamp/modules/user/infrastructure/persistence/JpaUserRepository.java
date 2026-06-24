package metro.ExoticStamp.modules.user.infrastructure.persistence;

import metro.ExoticStamp.modules.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserRepository extends JpaRepository<User, UUID> {

    Optional<User>  findByEmail(String email);
    Optional<User>  findByUsername(String username);
    boolean         existsByEmail(String email);
    boolean         existsByUsername(String username);
    boolean         existsByPhoneNumber(String phoneNumber);

    @Query("SELECT u.tokenVersion FROM User u WHERE u.id = :id")
    Optional<Long> findTokenVersionById(@Param("id") UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.tokenVersion = u.tokenVersion + 1 WHERE u.id = :id")
    int incrementTokenVersionById(@Param("id") UUID id);

    // Spring Data auto generates SQL Query - no need @Query
    // Do not import out of infrastructure/persistence
}
