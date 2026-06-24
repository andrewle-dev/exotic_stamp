package metro.ExoticStamp.modules.metro.domain.exception;

import java.util.UUID;

import lombok.Getter;

@Getter
public class DuplicateStationSequenceException extends RuntimeException {

    private final UUID lineId;
    private final Integer sequence;

    public DuplicateStationSequenceException(UUID lineId, Integer sequence) {
        super("Sequence " + sequence + " already exists on line " + lineId);
        this.lineId = lineId;
        this.sequence = sequence;
    }
}



