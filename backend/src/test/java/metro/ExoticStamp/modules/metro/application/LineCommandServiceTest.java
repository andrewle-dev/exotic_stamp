package metro.ExoticStamp.modules.metro.application;

import metro.ExoticStamp.modules.metro.application.command.CreateLineCommand;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.support.MetroAuditHelper;
import metro.ExoticStamp.modules.metro.application.view.LineView;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateLineCodeException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LineCommandServiceTest {

    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");

    @Mock private LineRepository lineRepository;
    @Mock private MetroAppMapper mapper;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private MetroAuditHelper metroAuditHelper;

    private LineCommandService service;

    @BeforeEach
    void setUp() {
        service = new LineCommandService(lineRepository, mapper, eventPublisher, metroAuditHelper);
    }

    @Test
    void createLine_success() {
        when(lineRepository.existsByCode("L1")).thenReturn(false);
        when(lineRepository.save(any(Line.class))).thenAnswer(inv -> {
            Line l = inv.getArgument(0);
            l.setId(LINE_ID);
            return l;
        });
        when(mapper.toLineView(any())).thenReturn(LineView.builder().id(LINE_ID).code("L1").build());

        service.createLine(CreateLineCommand.builder().code("L1").name("Line 1").build());
        verify(lineRepository).save(any(Line.class));
    }

    @Test
    void createLine_duplicateCode_throws() {
        when(lineRepository.existsByCode("L1")).thenReturn(true);
        assertThrows(DuplicateLineCodeException.class, () ->
                service.createLine(CreateLineCommand.builder().code("L1").name("Line 1").build()));
    }

    @Test
    void deleteLine_setsInactive() {
        Line line = Line.builder().id(LINE_ID).code("L1").name("Line").sortOrder(0)
                .totalStations(0).status(MetroStatus.ACTIVE).createdAt(LocalDateTime.now()).build();
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(line));
        when(lineRepository.save(any(Line.class))).thenAnswer(inv -> inv.getArgument(0));

        service.deleteLine(LINE_ID);
        verify(lineRepository).save(any(Line.class));
    }
}
