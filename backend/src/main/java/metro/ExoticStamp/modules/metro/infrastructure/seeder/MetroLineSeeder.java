package metro.ExoticStamp.modules.metro.infrastructure.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(50)
@RequiredArgsConstructor
@Slf4j
public class MetroLineSeeder implements CommandLineRunner {

    private static final String LINE_CODE = "M1";

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;

    @Override
    public void run(String... args) {
        LocalDateTime now = LocalDateTime.now();
        Line line;
        if (lineRepository.existsByCode(LINE_CODE)) {
            line = lineRepository.findByCode(LINE_CODE).orElseThrow();
            log.debug("[MetroLineSeeder] line {} already exists (id={})", LINE_CODE, line.getId());
        } else {
            line = lineRepository.save(Line.builder()
                    .code(LINE_CODE)
                    .name("Metro Line 1 (Ben Thanh - Suoi Tien)")
                    .displayName("Metro Line 1")
                    .colorHex("#1F6FB2")
                    .sortOrder(1)
                    .totalStations(0)
                    .status(MetroStatus.ACTIVE)
                    .createdAt(now)
                    .build());
            log.info("[MetroLineSeeder] inserted line {}", LINE_CODE);
        }

        int inserted = 0;
        int skipped = 0;
        for (StationSeed s : stationSeeds()) {
            if (stationRepository.existsByLineIdAndCode(line.getId(), s.code())) {
                skipped++;
                continue;
            }
            Station st = Station.builder()
                    .lineId(line.getId())
                    .code(s.code())
                    .name(s.name())
                    .displayName("Ga " + s.name())
                    .sortOrder(s.sortOrder())
                    .description(s.description())
                    .latitude(s.lat())
                    .longitude(s.lon())
                    .zoneRadiusMeters(150)
                    .nfcTagId(s.nfc())
                    .qrCodeValue(s.qr())
                    .scanKeyStatus(ScanKeyStatus.ACTIVE)
                    .lastScanKeyUpdatedAt(now)
                    .collectorCount(0)
                    .status(MetroStatus.ACTIVE)
                    .createdAt(now)
                    .build();
            stationRepository.save(st);
            inserted++;
        }

        int activeOnLine = stationRepository.findAllByLineIdAndStatus(line.getId(), MetroStatus.ACTIVE).size();
        line.setTotalStations(activeOnLine);
        line.setUpdatedAt(now);
        lineRepository.save(line);

        log.info("[MetroLineSeeder] summary line={} stationsInserted={} stationsSkippedAlready={} totalActiveOnLine={}",
                LINE_CODE, inserted, skipped, activeOnLine);
    }

    private static List<StationSeed> stationSeeds() {
        List<StationSeed> list = new ArrayList<>();
        list.add(seed("M1-S01", "Ben Thanh", 1, "Metro Line 1 station", "10.772000", "106.698300", "M1-NFC-001", "M1-QR-001"));
        list.add(seed("M1-S02", "Opera House", 2, "Metro Line 1 station", "10.777560", "106.703860", "M1-NFC-002", "M1-QR-002"));
        list.add(seed("M1-S03", "Ba Son", 3, "Metro Line 1 station", "10.786840", "106.706270", "M1-NFC-003", "M1-QR-003"));
        list.add(seed("M1-S04", "Van Thanh Park", 4, "Metro Line 1 station", "10.801200", "106.720200", "M1-NFC-004", "M1-QR-004"));
        list.add(seed("M1-S05", "Tan Cang", 5, "Metro Line 1 station", "10.808350", "106.721900", "M1-NFC-005", "M1-QR-005"));
        list.add(seed("M1-S06", "Thao Dien", 6, "Metro Line 1 station", "10.801980", "106.733850", "M1-NFC-006", "M1-QR-006"));
        list.add(seed("M1-S07", "An Phu", 7, "Metro Line 1 station", "10.794230", "106.752640", "M1-NFC-007", "M1-QR-007"));
        list.add(seed("M1-S08", "Rach Chiec", 8, "Metro Line 1 station", "10.790720", "106.771870", "M1-NFC-008", "M1-QR-008"));
        list.add(seed("M1-S09", "Phuoc Long", 9, "Metro Line 1 station", "10.799470", "106.780990", "M1-NFC-009", "M1-QR-009"));
        list.add(seed("M1-S10", "Binh Thai", 10, "Metro Line 1 station", "10.808480", "106.792760", "M1-NFC-010", "M1-QR-010"));
        list.add(seed("M1-S11", "Thu Duc", 11, "Metro Line 1 station", "10.818130", "106.806120", "M1-NFC-011", "M1-QR-011"));
        list.add(seed("M1-S12", "High Tech Park", 12, "Metro Line 1 station", "10.841110", "106.809940", "M1-NFC-012", "M1-QR-012"));
        list.add(seed("M1-S13", "National University", 13, "Metro Line 1 station", "10.870490", "106.800570", "M1-NFC-013", "M1-QR-013"));
        list.add(seed("M1-S14", "Suoi Tien Bus Station", 14, "Metro Line 1 station", "10.879970", "106.804590", "M1-NFC-014", "M1-QR-014"));
        return list;
    }

    private static StationSeed seed(String code, String name, int sortOrder, String desc,
                                    String lat, String lon, String nfc, String qr) {
        return new StationSeed(code, name, sortOrder, desc, bd(lat), bd(lon), nfc, qr);
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private record StationSeed(
            String code,
            String name,
            int sortOrder,
            String description,
            BigDecimal lat,
            BigDecimal lon,
            String nfc,
            String qr
    ) {}
}
