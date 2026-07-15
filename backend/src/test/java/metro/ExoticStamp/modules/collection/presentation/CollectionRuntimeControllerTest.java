package metro.ExoticStamp.modules.collection.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import metro.ExoticStamp.common.exceptions.GlobalExceptionHandler;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.collection.CollectionWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.collection.application.service.CollectionCommandService;
import metro.ExoticStamp.modules.collection.application.service.CollectionQueryService;
import metro.ExoticStamp.modules.collection.application.view.CollectOutcomeStatus;
import metro.ExoticStamp.modules.collection.application.view.CollectStatusView;
import metro.ExoticStamp.modules.collection.application.view.CollectStampResultView;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.StampBookView;
import metro.ExoticStamp.modules.collection.application.view.UserStampView;
import metro.ExoticStamp.modules.collection.presentation.mapper.CollectionRuntimePresentationMapper;
import metro.ExoticStamp.modules.collection.presentation.request.RuntimeCollectStampRequest;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CollectionRuntimeController.class)
@Import({CollectionWebMvcTestSecurityConfig.class, CollectionRuntimePresentationMapper.class, GlobalExceptionHandler.class})
class CollectionRuntimeControllerTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID CAMPAIGN_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
    private static final UUID STAMP_ID = UUID.fromString("00000000-0000-0000-0000-000000000040");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CollectionCommandService commandService;
    @MockBean private CollectionQueryService queryService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private CustomAuthEntryPoint customAuthEntryPoint;
    @MockBean private CustomAccessDeniedHandler customAccessDeniedHandler;
    @MockBean private RoleQueryService roleQueryService;

    @Test
    @WithAnonymousUser
    void collect_unauthenticated_returns401() throws Exception {
        RuntimeCollectStampRequest req = validRequest();
        mockMvc.perform(post("/api/v1/collection/collect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void collect_authenticated_returns201() throws Exception {
        when(commandService.collect(any())).thenReturn(sampleResult());

        mockMvc.perform(post("/api/v1/collection/collect")
                        .with(user(USER_ID.toString()).password("n/a").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.stamp.stampId").value(STAMP_ID.toString()))
                .andExpect(jsonPath("$.data.isNew").value(true))
                .andExpect(jsonPath("$.data.scan.scanType").value("NFC"));
    }

    @Test
    void stampBook_authenticated_returns200() throws Exception {
        when(queryService.getStampBook(eq(USER_ID), eq(LINE_ID))).thenReturn(
                StampBookView.builder()
                        .lineId(LINE_ID)
                        .lineName("Line 1")
                        .campaignId(CAMPAIGN_ID)
                        .campaignName("Campaign")
                        .stations(List.of())
                        .progress(ProgressView.builder().lineId(LINE_ID).collected(0).total(5).percentage(0).build())
                        .build());

        mockMvc.perform(get("/api/v1/collection/stamp-book")
                        .param("lineId", LINE_ID.toString())
                        .with(user(USER_ID.toString()).password("n/a").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lineId").value(LINE_ID.toString()))
                .andExpect(jsonPath("$.data.progress.total").value(5));
    }

    @Test
    void stampBook_withoutLineId_authenticated_returns200() throws Exception {
        when(queryService.getStampBook(eq(USER_ID), isNull())).thenReturn(
                StampBookView.builder()
                        .lineId(LINE_ID)
                        .lineName("Line 1")
                        .campaignId(CAMPAIGN_ID)
                        .campaignName("Campaign")
                        .stations(List.of())
                        .progress(ProgressView.builder().lineId(LINE_ID).collected(0).total(5).percentage(0).build())
                        .build());

        mockMvc.perform(get("/api/v1/collection/stamp-book")
                        .with(user(USER_ID.toString()).password("n/a").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.campaignId").value(CAMPAIGN_ID.toString()));
    }

    @Test
    void progress_authenticated_returns200() throws Exception {
        when(queryService.getMyProgress(eq(USER_ID), eq(LINE_ID), isNull())).thenReturn(
                ProgressView.builder().lineId(LINE_ID).collected(2).total(10).percentage(20).build());

        mockMvc.perform(get("/api/v1/collection/progress")
                        .param("lineId", LINE_ID.toString())
                        .with(user(USER_ID.toString()).password("n/a").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.collected").value(2))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.percentage").value(20));
    }

    @Test
    @WithAnonymousUser
    void progress_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/collection/progress")
                        .param("lineId", LINE_ID.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void progress_fourteenOfFourteen_returnsCollectedAndTotal() throws Exception {
        when(queryService.getMyProgress(eq(USER_ID), eq(LINE_ID), isNull())).thenReturn(
                ProgressView.builder().lineId(LINE_ID).collected(14).total(14).percentage(100).build());

        mockMvc.perform(get("/api/v1/collection/progress")
                        .param("lineId", LINE_ID.toString())
                        .with(user(USER_ID.toString()).password("n/a").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.collected").value(14))
                .andExpect(jsonPath("$.data.total").value(14))
                .andExpect(jsonPath("$.data.percentage").value(100));
    }

    @Test
    void myStamps_authenticated_returns200() throws Exception {
        when(queryService.getMyStamps(eq(USER_ID), eq(LINE_ID), eq(0), eq(20)))
                .thenReturn(PageResponse.of(List.of(), 0, 0, 0, 20));

        mockMvc.perform(get("/api/v1/collection/my-stamps")
                        .param("lineId", LINE_ID.toString())
                        .with(user(USER_ID.toString()).password("n/a").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithAnonymousUser
    void collectStatus_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/collection/collect/status")
                        .param("idempotencyKey", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void collectStatus_success_returns200() throws Exception {
        UUID key = UUID.fromString("550e8400-e29b-41d4-a716-446655440099");
        when(queryService.getCollectStatus(eq(USER_ID), eq(key))).thenReturn(
                CollectStatusView.builder()
                        .status(CollectOutcomeStatus.SUCCESS)
                        .stamp(CollectStampResultView.StampInfo.builder()
                                .stampId(STAMP_ID)
                                .stationId(STATION_ID)
                                .stationName("Central")
                                .lineId(LINE_ID)
                                .campaignId(CAMPAIGN_ID)
                                .collectedAt(LocalDateTime.now())
                                .build())
                        .progress(ProgressView.builder().lineId(LINE_ID).collected(1).total(5).percentage(20).build())
                        .resolvedAt(LocalDateTime.now())
                        .build());

        mockMvc.perform(get("/api/v1/collection/collect/status")
                        .param("idempotencyKey", key.toString())
                        .with(user(USER_ID.toString()).password("n/a").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stamp.stampId").value(STAMP_ID.toString()));
    }

    @Test
    void collectStatus_notFound_returns200WithNotFoundStatus() throws Exception {
        UUID key = UUID.randomUUID();
        when(queryService.getCollectStatus(eq(USER_ID), eq(key))).thenReturn(
                CollectStatusView.builder().status(CollectOutcomeStatus.NOT_FOUND).build());

        mockMvc.perform(get("/api/v1/collection/collect/status")
                        .param("idempotencyKey", key.toString())
                        .with(user(USER_ID.toString()).password("n/a").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("NOT_FOUND"));
    }

    @Test
    void collectStatus_duplicate_returns200WithDuplicateStatus() throws Exception {
        UUID key = UUID.randomUUID();
        when(queryService.getCollectStatus(eq(USER_ID), eq(key))).thenReturn(
                CollectStatusView.builder()
                        .status(CollectOutcomeStatus.DUPLICATE)
                        .stamp(CollectStampResultView.StampInfo.builder()
                                .stampId(STAMP_ID)
                                .stationId(STATION_ID)
                                .stationName("Central")
                                .lineId(LINE_ID)
                                .campaignId(CAMPAIGN_ID)
                                .collectedAt(LocalDateTime.now())
                                .build())
                        .build());

        mockMvc.perform(get("/api/v1/collection/collect/status")
                        .param("idempotencyKey", key.toString())
                        .with(user(USER_ID.toString()).password("n/a").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DUPLICATE"));
    }

    private static RuntimeCollectStampRequest validRequest() {
        RuntimeCollectStampRequest req = new RuntimeCollectStampRequest();
        req.setScanType("NFC");
        req.setPayload("NFC-TAG-1");
        req.setLatitude(BigDecimal.valueOf(10.772));
        req.setLongitude(BigDecimal.valueOf(106.698));
        req.setAccuracyMeters(BigDecimal.valueOf(35));
        req.setDevicePlatform("ANDROID");
        req.setAppVersion("1.0.0");
        req.setIdempotencyKey(UUID.randomUUID());
        return req;
    }

    private static CollectStampResultView sampleResult() {
        return CollectStampResultView.builder()
                .stamp(CollectStampResultView.StampInfo.builder()
                        .stampId(STAMP_ID)
                        .stationId(STATION_ID)
                        .stationName("Central")
                        .lineName("Line 1")
                        .lineId(LINE_ID)
                        .campaignId(CAMPAIGN_ID)
                        .stampDesignUrl("https://cdn/x.png")
                        .collectedAt(LocalDateTime.now())
                        .build())
                .progress(ProgressView.builder().lineId(LINE_ID).collected(1).total(10).percentage(10).build())
                .scan(CollectStampResultView.ScanInfo.builder()
                        .scanType("NFC")
                        .gpsDistanceMeters(BigDecimal.TEN)
                        .gpsAccuracyMeters(BigDecimal.valueOf(35))
                        .build())
                .isNew(true)
                .build();
    }
}
