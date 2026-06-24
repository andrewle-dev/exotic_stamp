package metro.ExoticStamp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesignStatus;
import metro.ExoticStamp.modules.collection.domain.model.StampRarity;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.rbac.domain.model.Role;
import metro.ExoticStamp.modules.rbac.domain.model.RoleName;
import metro.ExoticStamp.modules.rbac.domain.model.RoleStatus;
import metro.ExoticStamp.modules.rbac.domain.model.UserRole;
import metro.ExoticStamp.modules.rbac.domain.repository.RoleRepository;
import metro.ExoticStamp.modules.rbac.domain.repository.UserRoleRepository;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.Partner;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import metro.ExoticStamp.modules.reward.domain.repository.PartnerRepository;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Dev-only MVP demo data: campaign stations, stamp designs, milestones, vouchers, test user.
 * Loaded after {@link metro.ExoticStamp.modules.metro.infrastructure.seeder.MetroLineSeeder}
 * and {@link metro.ExoticStamp.modules.collection.infrastructure.bootstrap.CollectionBootstrapper}.
 */
@Slf4j
@Component
@Profile("dev")
@Order(60)
@RequiredArgsConstructor
public class MvpDemoSeedBootstrap implements ApplicationRunner {

    static final String DEMO_PARTNER_NAME = "Demo Coffee Partner";
    static final String DEMO_USER_USERNAME = "mobiletest";
    static final String DEMO_USER_EMAIL = "mobiletest@exoticstamp.local";
    static final String DEMO_LINE_CODE = "M1";
    private static final int DEMO_STATION_COUNT = 5;
    private static final int VOUCHERS_PER_MILESTONE = 10;

    private final LineRepository lineRepository;
    private final StationReadPort stationReadPort;
    private final CampaignRepository campaignRepository;
    private final CampaignStationRepository campaignStationRepository;
    private final StampDesignRepository stampDesignRepository;
    private final PartnerRepository partnerRepository;
    private final MilestoneRepository milestoneRepository;
    private final VoucherPoolRepository voucherPoolRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.bootstrap.demo-user-password:changeme-demo-only}")
    private String demoUserPassword;

    @Override
    public void run(ApplicationArguments args) {
        Line line = lineRepository.findByCode(DEMO_LINE_CODE).orElse(null);
        if (line == null || line.getId() == null) {
            log.warn("[MvpDemoSeed] skip — line {} not found (run MetroLineSeeder first)", DEMO_LINE_CODE);
            return;
        }
        UUID lineId = line.getId();
        var campaign = campaignRepository.findDefaultByLineId(lineId).orElse(null);
        if (campaign == null || campaign.getId() == null) {
            log.warn("[MvpDemoSeed] skip — default campaign missing for line {}", DEMO_LINE_CODE);
            return;
        }
        UUID campaignId = campaign.getId();

        List<MetroStationView> stations = stationReadPort.listActiveStationsByLineId(lineId).stream()
                .sorted((a, b) -> Integer.compare(
                        a.sequence() != null ? a.sequence() : 0,
                        b.sequence() != null ? b.sequence() : 0))
                .limit(DEMO_STATION_COUNT)
                .toList();

        int assigned = 0;
        int designs = 0;
        for (MetroStationView station : stations) {
            if (!campaignStationRepository.exists(campaignId, station.id())) {
                campaignStationRepository.assign(campaignId, station.id());
                assigned++;
            }
            if (!stampDesignRepository.existsActiveByCampaignIdAndStationId(campaignId, station.id())) {
                LocalDateTime now = LocalDateTime.now();
                stampDesignRepository.save(StampDesign.builder()
                        .campaignId(campaignId)
                        .stationId(station.id())
                        .name("Stamp — " + station.name())
                        .description("Demo stamp for " + station.name())
                        .imageUrl("/uploads/public/stamps/demo-" + station.id() + ".png")
                        .previewImageUrl("/uploads/public/stamps/demo-" + station.id() + "-thumb.png")
                        .rarity(StampRarity.COMMON)
                        .status(StampDesignStatus.ACTIVE)
                        .sortOrder(station.sequence() != null ? station.sequence() : 0)
                        .createdAt(now)
                        .build());
                designs++;
            }
        }

        Partner partner = seedPartner();
        int milestones = seedMilestones(lineId, campaignId, partner.getId());

        seedDemoUser();

        log.info("[MvpDemoSeed] line={} campaignId={} stationsAssigned={} stampDesignsAdded={} milestones={} partnerId={}",
                DEMO_LINE_CODE, campaignId, assigned, designs, milestones, partner.getId());
        log.info("[MvpDemoSeed] NFC test payloads: M1-NFC-001 .. M1-NFC-00{} | QR: M1-QR-001 .. M1-QR-00{}",
                DEMO_STATION_COUNT, DEMO_STATION_COUNT);
        log.info("[MvpDemoSeed] Test user: {} / {} (password from application.bootstrap.demo-user-password)",
                DEMO_USER_USERNAME, DEMO_USER_EMAIL);
    }

    private Partner seedPartner() {
        LocalDateTime now = LocalDateTime.now();
        return partnerRepository.findAllPaged(true, 0, 50).content().stream()
                .filter(p -> DEMO_PARTNER_NAME.equals(p.getName()))
                .findFirst()
                .orElseGet(() -> partnerRepository.save(Partner.builder()
                        .name(DEMO_PARTNER_NAME)
                        .logoUrl("/uploads/public/partners/demo-coffee.png")
                        .contactEmail("partner@demo.local")
                        .contractStartDate(LocalDate.now().minusMonths(1))
                        .contractEndDate(LocalDate.now().plusYears(1))
                        .active(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()));
    }

    private int seedMilestones(UUID lineId, UUID campaignId, UUID partnerId) {
        int created = 0;
        created += ensureMilestone(lineId, campaignId, partnerId, "M1", 1, "First Stamp Badge", RewardType.DIGITAL_BADGE, 1);
        created += ensureMilestone(lineId, campaignId, partnerId, "M3", 3, "3-Stamp Voucher", RewardType.VOUCHER, 2);
        created += ensureMilestone(lineId, campaignId, partnerId, "M5", 5, "5-Stamp Grand Prize", RewardType.VOUCHER, 3);
        return created;
    }

    private int ensureMilestone(
            UUID lineId,
            UUID campaignId,
            UUID partnerId,
            String code,
            int stampsRequired,
            String title,
            RewardType rewardType,
            int sortOrder
    ) {
        if (milestoneRepository.findActiveByCampaignId(campaignId).stream()
                .anyMatch(m -> code.equalsIgnoreCase(m.getCode()))) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        Milestone milestone = milestoneRepository.save(Milestone.builder()
                .lineId(lineId)
                .campaignId(campaignId)
                .code(code)
                .stampsRequired(stampsRequired)
                .name(title)
                .description("Demo milestone — collect " + stampsRequired + " stamps")
                .rewardType(rewardType)
                .rewardTitle(title)
                .rewardDescription("MVP demo reward")
                .rewardImageUrl("/uploads/public/rewards/demo-" + code.toLowerCase() + ".png")
                .status(MilestoneStatus.ACTIVE)
                .sortOrder(sortOrder)
                .createdAt(now)
                .updatedAt(now)
                .build());

        if (rewardType == RewardType.VOUCHER && milestone.getId() != null) {
            seedVoucherPool(milestone.getId(), code);
        }
        return 1;
    }

    private void seedVoucherPool(UUID milestoneId, String code) {
        if (voucherPoolRepository.countAvailableByMilestoneId(milestoneId) > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<VoucherPool> batch = new java.util.ArrayList<>();
        for (int i = 1; i <= VOUCHERS_PER_MILESTONE; i++) {
            batch.add(VoucherPool.builder()
                    .milestoneId(milestoneId)
                    .code("DEMO-" + code + "-" + String.format("%03d", i))
                    .status(VoucherPoolStatus.AVAILABLE)
                    .createdAt(now)
                    .build());
        }
        voucherPoolRepository.saveAll(batch);
    }

    private void seedDemoUser() {
        User user = userRepository.findByUsername(DEMO_USER_USERNAME)
                .orElseGet(() -> userRepository.save(User.builder()
                        .firstname("Mobile")
                        .lastname("Tester")
                        .username(DEMO_USER_USERNAME)
                        .email(DEMO_USER_EMAIL)
                        .phoneNumber("+84909999001")
                        .password(passwordEncoder.encode(demoUserPassword))
                        .status(UserStatus.ACTIVE)
                        .verifiedAt(LocalDateTime.now())
                        .build()));

        Role userRole = roleRepository.findByRoleName(RoleName.USER)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .role(RoleName.USER.name())
                        .status(RoleStatus.ACTIVE)
                        .systemRole(true)
                        .build()));

        UUID userId = user.getId();
        UUID roleId = userRole.getId();
        if (userId != null && roleId != null
                && !userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            userRoleRepository.save(UserRole.builder()
                    .userId(userId)
                    .role(userRole)
                    .build());
        }
    }
}
