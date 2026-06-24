package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.application.command.CreateLineCommand;
import metro.ExoticStamp.modules.metro.application.command.UpdateLineCommand;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.support.MetroAuditHelper;
import metro.ExoticStamp.modules.metro.application.support.MetroEnumParser;
import metro.ExoticStamp.modules.metro.application.view.LineView;
import metro.ExoticStamp.modules.metro.domain.event.LineCreatedEvent;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateLineCodeException;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LineCommandService {

    private final LineRepository lineRepository;
    private final MetroAppMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final MetroAuditHelper metroAuditHelper;

    @Transactional
    public LineView createLine(CreateLineCommand command) {
        if (lineRepository.existsByCode(command.getCode())) {
            throw new DuplicateLineCodeException(command.getCode());
        }
        LocalDateTime now = LocalDateTime.now();
        MetroStatus status = MetroEnumParser.parseStatus(command.getStatus());
        if (status == null) {
            status = MetroStatus.DRAFT;
        }
        Line line = Line.builder()
                .code(command.getCode().trim())
                .name(command.getName().trim())
                .displayName(blankToNull(command.getDisplayName()))
                .description(blankToNull(command.getDescription()))
                .colorHex(blankToNull(command.getColorHex()))
                .sortOrder(command.getSortOrder() != null ? command.getSortOrder() : 0)
                .totalStations(0)
                .status(status)
                .createdAt(now)
                .build();
        Line saved = lineRepository.save(line);
        RbacTransactionCallbacks.afterCommit(() -> eventPublisher.publishEvent(new LineCreatedEvent(saved.getId())));
        metroAuditHelper.scheduleLineCreated(saved.getId().toString());
        return mapper.toLineView(saved);
    }

    @Transactional
    public LineView updateLine(UpdateLineCommand command) {
        Line line = lineRepository.findById(command.getLineId())
                .orElseThrow(() -> new LineNotFoundException(command.getLineId()));
        if (command.getCode() != null && !command.getCode().isBlank()) {
            String code = command.getCode().trim();
            if (!code.equals(line.getCode()) && lineRepository.existsByCodeAndIdNot(code, command.getLineId())) {
                throw new DuplicateLineCodeException(code);
            }
            line.setCode(code);
        }
        if (command.getName() != null && !command.getName().isBlank()) {
            line.setName(command.getName().trim());
        }
        if (command.getDisplayName() != null) {
            line.setDisplayName(blankToNull(command.getDisplayName()));
        }
        if (command.getDescription() != null) {
            line.setDescription(blankToNull(command.getDescription()));
        }
        if (command.getColorHex() != null) {
            line.setColorHex(blankToNull(command.getColorHex()));
        }
        if (command.getSortOrder() != null) {
            line.setSortOrder(command.getSortOrder());
        }
        if (command.getStatus() != null) {
            line.setStatus(MetroEnumParser.parseStatus(command.getStatus()));
        }
        line.setUpdatedAt(LocalDateTime.now());
        Line saved = lineRepository.save(line);
        metroAuditHelper.scheduleLineUpdated(saved.getId().toString());
        return mapper.toLineView(saved);
    }

    @Transactional
    public void deleteLine(UUID lineId) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        if (line.getStatus() == MetroStatus.INACTIVE) {
            return;
        }
        line.setStatus(MetroStatus.INACTIVE);
        line.setUpdatedAt(LocalDateTime.now());
        lineRepository.save(line);
        metroAuditHelper.scheduleLineDisabled(lineId.toString());
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
