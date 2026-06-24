package metro.ExoticStamp.modules.metro.application.port;

import metro.ExoticStamp.modules.metro.application.view.MetroLineView;

import java.util.List;
import java.util.UUID;

public interface LineReadPort {

    List<MetroLineView> getAllActiveLines();

    MetroLineView getLineById(UUID lineId);
}
