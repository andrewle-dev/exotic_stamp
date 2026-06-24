package metro.ExoticStamp.modules.metro.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToggleLineStatusCommand {
    private UUID lineId;
    private Boolean isActive;
}
