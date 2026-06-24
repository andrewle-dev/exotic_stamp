package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.MetroPageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LineRepositoryAdapter implements LineRepository {

    private final JpaLineRepository jpaLineRepository;

    @Override
    public List<Line> findAll() {
        return jpaLineRepository.findAll();
    }

    @Override
    public List<Line> findAllByIdIn(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return jpaLineRepository.findAllById(ids);
    }

    @Override
    public List<Line> findAllByStatus(MetroStatus status) {
        return jpaLineRepository.findAllByStatusOrderBySortOrderAsc(status);
    }

    @Override
    public Optional<Line> findById(UUID id) {
        return jpaLineRepository.findById(id);
    }

    @Override
    public Line save(Line line) {
        return jpaLineRepository.save(line);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaLineRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, UUID id) {
        return jpaLineRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public Optional<Line> findByCode(String code) {
        return jpaLineRepository.findByCode(code);
    }

    @Override
    public MetroPageResult<Line> search(MetroStatus status, String search, int page, int size, String sortField, boolean ascending) {
        int effectiveSize = size <= 0 ? 20 : Math.min(size, 100);
        Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, mapSortField(sortField));
        PageRequest pageable = PageRequest.of(page, effectiveSize, sort);
        Page<Line> result = jpaLineRepository.search(status, blankToNull(search), pageable);
        return new MetroPageResult<>(result.getContent(), result.getTotalElements(), result.getTotalPages(),
                result.getNumber(), result.getSize());
    }

    private static String mapSortField(String sortField) {
        if (sortField == null || sortField.isBlank()) {
            return "sortOrder";
        }
        return switch (sortField) {
            case "code" -> "code";
            case "name" -> "name";
            case "status" -> "status";
            case "createdAt" -> "createdAt";
            default -> "sortOrder";
        };
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
