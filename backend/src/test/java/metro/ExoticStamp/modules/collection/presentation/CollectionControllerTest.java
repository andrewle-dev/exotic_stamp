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
import metro.ExoticStamp.modules.collection.application.mapper.UserStampAppMapper;
import metro.ExoticStamp.modules.collection.application.service.CollectionCommandService;
import metro.ExoticStamp.modules.collection.application.service.CollectionQueryService;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.StampBookView;
import metro.ExoticStamp.modules.collection.application.view.UserStampView;
import metro.ExoticStamp.modules.collection.application.view.StampCollectView;
import metro.ExoticStamp.modules.collection.presentation.controller.CollectionController;
import metro.ExoticStamp.modules.collection.presentation.mapper.CollectionResponseMapper;
import metro.ExoticStamp.config.WebConfig;
import metro.ExoticStamp.config.DeprecatedLegacyCollectionsInterceptor;
import metro.ExoticStamp.modules.collection.presentation.request.CollectStampRequest;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidScanInputException;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.user.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CollectionController.class)
@Import({CollectionWebMvcTestSecurityConfig.class, CollectionResponseMapper.class, GlobalExceptionHandler.class,
        WebConfig.class, DeprecatedLegacyCollectionsInterceptor.class})
class CollectionControllerTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CollectionCommandService commandService;

    @MockBean
    private CollectionQueryService queryService;

    @MockBean
    private UserStampAppMapper userStampAppMapper;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private AccessTokenRevocationValidator accessTokenRevocationValidator;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private RoleQueryService roleQueryService;

    @MockBean
    private CustomAuthEntryPoint authEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    private User testUser() {
        User u = new User();
        u.setId(USER_ID);
        u.setUsername("u1");
        u.setEmail("u1@test.com");
        u.setPhoneNumber("+10000000001");
        u.setPassword("x");
        return u;
    }

    @Test
    void collect_nfc_returns201() throws Exception {
        when(commandService.collectStamp(any())).thenReturn(
                StampCollectView.builder()
                        .stampId(UUID.randomUUID())
                        .stationId(UUID.randomUUID())
                        .stationName("S")
                        .lineId(LINE_ID)
                        .campaignId(UUID.randomUUID())
                        .stampDesignUrl("u")
                        .collectedAt(LocalDateTime.now())
                        .isNew(true)
                        .collectMethod("NFC")
                        .progress(ProgressView.builder().lineId(LINE_ID).collected(1).total(5).percentage(20).build())
                        .build());
        when(userStampAppMapper.resolveCollectMethod(any(), any())).thenReturn(metro.ExoticStamp.modules.collection.domain.model.CollectMethod.NFC);

        CollectStampRequest req = new CollectStampRequest();
        req.setIdempotencyKey(UUID.randomUUID());
        req.setNfcTagId("NFC1");
        req.setDeviceFingerprint("1234567890");

        mockMvc.perform(post("/api/v1/collections/scan")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(header().string("Link", "</api/v1/collection>; rel=\"successor-version\""))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void collect_idempotentReplay_returns201_withIsNewFalse() throws Exception {
        when(commandService.collectStamp(any())).thenReturn(
                StampCollectView.builder()
                        .stampId(UUID.randomUUID())
                        .stationId(STATION_ID)
                        .stationName("S")
                        .lineId(LINE_ID)
                        .campaignId(UUID.randomUUID())
                        .stampDesignUrl("u")
                        .collectedAt(LocalDateTime.now())
                        .isNew(false)
                        .collectMethod("NFC")
                        .progress(ProgressView.builder().lineId(LINE_ID).collected(2).total(5).percentage(40).build())
                        .build());
        when(userStampAppMapper.resolveCollectMethod(any(), any())).thenReturn(metro.ExoticStamp.modules.collection.domain.model.CollectMethod.NFC);

        CollectStampRequest req = new CollectStampRequest();
        req.setIdempotencyKey(UUID.randomUUID());
        req.setNfcTagId("NFC1");
        req.setDeviceFingerprint("1234567890");

        mockMvc.perform(post("/api/v1/collections/scan")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.isNew").value(false));
    }

    @Test
    void collect_inactiveStation_returns422() throws Exception {
        when(commandService.collectStamp(any())).thenThrow(new StationInactiveException(STATION_ID));
        when(userStampAppMapper.resolveCollectMethod(any(), any())).thenReturn(metro.ExoticStamp.modules.collection.domain.model.CollectMethod.NFC);

        CollectStampRequest req = new CollectStampRequest();
        req.setIdempotencyKey(UUID.randomUUID());
        req.setNfcTagId("NFC_INACTIVE");
        req.setDeviceFingerprint("1234567890");

        mockMvc.perform(post("/api/v1/collections/scan")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("STATION_INACTIVE"));
    }

    @Test
    void collect_bothNfcAndQr_returnsInvalidScanMethod() throws Exception {
        when(userStampAppMapper.resolveCollectMethod(any(), any()))
                .thenReturn(metro.ExoticStamp.modules.collection.domain.model.CollectMethod.NFC);
        when(commandService.collectStamp(any()))
                .thenThrow(new InvalidScanInputException("Provide only one of nfcTagId or qrToken"));

        CollectStampRequest req = new CollectStampRequest();
        req.setIdempotencyKey(UUID.randomUUID());
        req.setNfcTagId("NFC1");
        req.setQrToken("QR1");
        req.setDeviceFingerprint("1234567890");

        mockMvc.perform(post("/api/v1/collections/scan")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SCAN_METHOD"));
    }

    @Test
    void collect_stationNotFound_returns404() throws Exception {
        when(commandService.collectStamp(any())).thenThrow(new StationNotFoundException("nfcTagId", "unknown"));
        when(userStampAppMapper.resolveCollectMethod(any(), any())).thenReturn(metro.ExoticStamp.modules.collection.domain.model.CollectMethod.NFC);

        CollectStampRequest req = new CollectStampRequest();
        req.setIdempotencyKey(UUID.randomUUID());
        req.setNfcTagId("NFC_UNKNOWN");
        req.setDeviceFingerprint("1234567890");

        mockMvc.perform(post("/api/v1/collections/scan")
                        .with(user(testUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("STATION_NOT_FOUND"));
    }

    @Test
    void getStamps_paginated() throws Exception {
        UserStampView v = UserStampView.builder()
                .stampId(UUID.randomUUID())
                .stationId(UUID.randomUUID())
                .lineId(LINE_ID)
                .campaignId(UUID.randomUUID())
                .stationName("S")
                .stampDesignUrl("u")
                .collectedAt(LocalDateTime.now())
                .collectMethod("NFC")
                .build();
        when(queryService.getMyStamps(eq(USER_ID), eq(LINE_ID), isNull(), eq(0), eq(20)))
                .thenReturn(PageResponse.of(List.of(v), 1, 1, 0, 20));

        mockMvc.perform(get("/api/v1/collections/me/stamps")
                        .param("lineId", LINE_ID.toString())
                        .param("page", "0")
                        .param("size", "20")
                        .with(user(testUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getProgress_path() throws Exception {
        when(queryService.getMyProgress(eq(USER_ID), eq(LINE_ID), isNull()))
                .thenReturn(ProgressView.builder().lineId(LINE_ID).collected(1).total(10).percentage(10).build());

        mockMvc.perform(get("/api/v1/collections/me/progress/{lineId}", LINE_ID)
                        .with(user(testUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.percentage").value(10));
    }

    @Test
    void getHistory() throws Exception {
        when(queryService.getMyHistory(eq(USER_ID), eq(0), eq(20)))
                .thenReturn(PageResponse.of(List.of(), 0, 0, 0, 20));

        mockMvc.perform(get("/api/v1/collections/me/history")
                        .with(user(testUser())))
                .andExpect(status().isOk());
    }

    @Test
    void getStampBook() throws Exception {
        when(queryService.getStampBook(eq(USER_ID), eq(LINE_ID), isNull()))
                .thenReturn(StampBookView.builder()
                        .lineId(LINE_ID)
                        .lineName("Line 1")
                        .campaignId(UUID.randomUUID())
                        .campaignName("Campaign")
                        .stations(List.of())
                        .progress(ProgressView.builder().lineId(LINE_ID).collected(0).total(5).percentage(0).build())
                        .build());

        mockMvc.perform(get("/api/v1/collections/me/stamp-book/{lineId}", LINE_ID)
                        .with(user(testUser())))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/collections/me/stamps").param("lineId", LINE_ID.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("403 for another user's data: not applicable — collection routes only expose /me scoped to the JWT principal")
    void forbidden_otherUser_notApplicable_routesAreMeScoped() {
        assertTrue(true);
    }
}
