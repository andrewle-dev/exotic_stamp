package metro.ExoticStamp.modules.collection.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignCodeDuplicateException;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignStationDuplicateException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CampaignRepositoryAdapter implements CampaignRepository {

    private final JpaCampaignRepository jpaCampaignRepository;

    @Override
    public Optional<Campaign> findById(UUID id) {
        return jpaCampaignRepository.findById(id);
    }

    @Override
    public Optional<Campaign> findByIdNotDeleted(UUID id) {
        return jpaCampaignRepository.findById(id).filter(c -> !c.isDeleted());
    }

    @Override
    public Campaign save(Campaign campaign) {
        try {
            return jpaCampaignRepository.save(campaign);
        } catch (DataIntegrityViolationException ex) {
            if (isCampaignCodeViolation(ex)) {
                throw new CampaignCodeDuplicateException(campaign.getCode());
            }
            throw ex;
        }
    }

    @Override
    public boolean existsDefaultByLineId(UUID lineId) {
        return jpaCampaignRepository.existsByLineIdAndIsDefaultTrue(lineId);
    }

    @Override
    public Optional<Campaign> findDefaultByLineId(UUID lineId) {
        return jpaCampaignRepository.findDefaultByLineId(lineId);
    }

    @Override
    public List<Campaign> findAllActiveDefaults() {
        return jpaCampaignRepository.findAllActiveDefaults();
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaCampaignRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, UUID id) {
        return jpaCampaignRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public PageResult<Campaign> findAllPaged(int page, int size) {
        Page<Campaign> p = jpaCampaignRepository.findAll(PageRequest.of(page, size));
        return PageResult.of(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber());
    }

    @Override
    public PageResult<Campaign> findAllNotDeletedPaged(int page, int size) {
        Page<Campaign> p = jpaCampaignRepository.findAllNotDeleted(PageRequest.of(page, size));
        return PageResult.of(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber());
    }

    @Override
    public List<Campaign> findByLineIdOrderByCodeAsc(UUID lineId) {
        return jpaCampaignRepository.findByLineIdOrderByCodeAsc(lineId);
    }

    @Override
    public List<Campaign> findActiveInWindow(LocalDateTime now) {
        return jpaCampaignRepository.findActiveInWindow(now);
    }

    @Override
    public List<Campaign> findActiveByStationId(UUID stationId, LocalDateTime now) {
        return jpaCampaignRepository.findActiveByStationId(stationId, now);
    }

    private static boolean isCampaignCodeViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        return msg != null && msg.contains("uq_campaigns_code");
    }
}
