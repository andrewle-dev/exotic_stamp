package metro.ExoticStamp.modules.metro.presentation.dto.request;

import lombok.Data;
import metro.ExoticStamp.modules.metro.presentation.dto.ScanKeyStatusApi;

@Data
public class UpdateScanKeysRequest {
    private String nfcTagId;
    private String qrCodeValue;
    private ScanKeyStatusApi scanKeyStatus;
}
