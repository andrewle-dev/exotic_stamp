package metro.ExoticStamp.modules.metro.domain.repository;

import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LineRepository {

    List<Line> findAll();

    List<Line> findAllByIdIn(Collection<UUID> ids);

    List<Line> findAllByStatus(MetroStatus status);

    Optional<Line> findById(UUID id);

    Line save(Line line);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    Optional<Line> findByCode(String code);

    MetroPageResult<Line> search(MetroStatus status, String search, int page, int size, String sortField, boolean ascending);
}
