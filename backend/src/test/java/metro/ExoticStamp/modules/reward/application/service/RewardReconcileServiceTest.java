package metro.ExoticStamp.modules.reward.application.service;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import metro.ExoticStamp.modules.reward.application.port.RewardReconcileCandidatePort;
import metro.ExoticStamp.modules.reward.application.port.RewardReconcileLockPort;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardReconcileServiceTest {

    @Mock private RewardReconcileCandidatePort candidatePort;
    @Mock private RewardReconcileLockPort lockPort;
    @Mock private RewardEvaluationService rewardEvaluationService;

    private RewardReconcileService service;

    @BeforeEach
    void setUp() {
        RewardProperties properties = new RewardProperties();
        properties.setReconcileLookback(Duration.ofHours(48));
        properties.setReconcileBatchSize(10);
        properties.setReconcileMaxDuration(Duration.ofSeconds(30));
        properties.setReconcileMaxBatches(3);
        properties.setReconcileMaxBatchSize(100);
        service = new RewardReconcileService(
                candidatePort, lockPort, rewardEvaluationService, properties, new SimpleMeterRegistry());
        lenient().when(lockPort.tryAcquire()).thenReturn(true);
    }

    @Test
    void runReconcile_evaluatesMissingPairs() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        when(candidatePort.findMissingRewardCandidates(eq(Duration.ofHours(48)), eq(10)))
                .thenReturn(List.of(new RewardReconcileCandidatePort.UserCampaignPair(userId, campaignId)))
                .thenReturn(List.of());
        when(candidatePort.claimPendingStockRewardIds(any(), anyInt())).thenReturn(List.of());

        var result = service.runReconcile();

        assertFalse(result.skipped());
        assertEquals(1, result.missingExamined());
        assertEquals(1, result.missingRepaired());
        verify(rewardEvaluationService).handleStampCollected(userId, campaignId);
        verify(lockPort).release();
    }

    @Test
    void runReconcile_fulfillsPendingStock() {
        UUID rewardId = UUID.randomUUID();
        when(candidatePort.findMissingRewardCandidates(any(), anyInt())).thenReturn(List.of());
        when(candidatePort.claimPendingStockRewardIds(any(), anyInt()))
                .thenReturn(List.of(rewardId))
                .thenReturn(List.of());
        when(rewardEvaluationService.fulfillPendingStock(rewardId))
                .thenReturn(RewardEvaluationService.PendingStockResult.FULFILLED);

        var result = service.runReconcile();
        assertEquals(1, result.pendingFulfilled());
        verify(rewardEvaluationService).fulfillPendingStock(rewardId);
    }

    @Test
    void runReconcile_busyWhenLockNotAcquired() {
        when(lockPort.tryAcquire()).thenReturn(false);
        var result = service.runReconcile();
        assertTrue(result.skipped());
        assertEquals("already-running", result.skipReason());
        verify(rewardEvaluationService, never()).handleStampCollected(any(), any());
    }

    @Test
    void dryRun_doesNotMutate() {
        when(candidatePort.findMissingRewardCandidates(any(), anyInt()))
                .thenReturn(List.of(new RewardReconcileCandidatePort.UserCampaignPair(UUID.randomUUID(), UUID.randomUUID())));
        when(candidatePort.peekPendingStockRewardIds(any(), anyInt())).thenReturn(List.of(UUID.randomUUID()));

        var result = service.runReconcile(RewardReconcileService.ReconcileRequest.admin(5, true, UUID.randomUUID()));
        assertEquals("dry-run", result.skipReason());
        verify(rewardEvaluationService, never()).handleStampCollected(any(), any());
        verify(rewardEvaluationService, never()).fulfillPendingStock(any());
    }

    @Test
    void runReconcile_skippedDuringShutdown() {
        service.onShutdown();
        var result = service.runReconcile();
        assertTrue(result.skipped());
        assertEquals("shutdown", result.skipReason());
        verify(lockPort, never()).tryAcquire();
    }

    @Test
    void scheduledReconcile_delegatesToRunReconcile() {
        when(candidatePort.findMissingRewardCandidates(any(), anyInt())).thenReturn(List.of());
        when(candidatePort.claimPendingStockRewardIds(any(), anyInt())).thenReturn(List.of());

        service.scheduledReconcile();

        verify(candidatePort).findMissingRewardCandidates(eq(Duration.ofHours(48)), eq(10));
        verify(lockPort).release();
    }

    @Test
    void runReconcile_missingCandidateFailureDoesNotAbortBatch() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        when(candidatePort.findMissingRewardCandidates(any(), anyInt()))
                .thenReturn(List.of(
                        new RewardReconcileCandidatePort.UserCampaignPair(user1, campaignId),
                        new RewardReconcileCandidatePort.UserCampaignPair(user2, campaignId)))
                .thenReturn(List.of());
        when(candidatePort.claimPendingStockRewardIds(any(), anyInt())).thenReturn(List.of());
        doThrow(new RuntimeException("boom"))
                .when(rewardEvaluationService).handleStampCollected(user1, campaignId);

        var result = service.runReconcile();

        assertEquals(2, result.missingExamined());
        assertEquals(1, result.missingRepaired());
        assertEquals(1, result.failed());
        verify(rewardEvaluationService).handleStampCollected(user2, campaignId);
    }

    @Test
    void runReconcile_stillNoStockCounted() {
        UUID rewardId = UUID.randomUUID();
        when(candidatePort.findMissingRewardCandidates(any(), anyInt())).thenReturn(List.of());
        when(candidatePort.claimPendingStockRewardIds(any(), anyInt()))
                .thenReturn(List.of(rewardId))
                .thenReturn(List.of());
        when(rewardEvaluationService.fulfillPendingStock(rewardId))
                .thenReturn(RewardEvaluationService.PendingStockResult.STILL_NO_STOCK);

        var result = service.runReconcile();

        assertEquals(1, result.pendingExamined());
        assertEquals(0, result.pendingFulfilled());
        assertEquals(1, result.stillNoStock());
    }

    @Test
    void runReconcile_skippedPendingCounted() {
        UUID rewardId = UUID.randomUUID();
        when(candidatePort.findMissingRewardCandidates(any(), anyInt())).thenReturn(List.of());
        when(candidatePort.claimPendingStockRewardIds(any(), anyInt()))
                .thenReturn(List.of(rewardId))
                .thenReturn(List.of());
        when(rewardEvaluationService.fulfillPendingStock(rewardId))
                .thenReturn(RewardEvaluationService.PendingStockResult.SKIPPED);

        var result = service.runReconcile();

        assertEquals(1, result.pendingExamined());
        assertEquals(0, result.pendingFulfilled());
        assertEquals(0, result.stillNoStock());
    }

    @Test
    void runReconcile_clampsNullBatchSizeToConfigured() {
        when(candidatePort.findMissingRewardCandidates(any(), eq(10))).thenReturn(List.of());
        when(candidatePort.claimPendingStockRewardIds(any(), eq(10))).thenReturn(List.of());

        service.runReconcile(RewardReconcileService.ReconcileRequest.scheduled());

        verify(candidatePort).findMissingRewardCandidates(Duration.ofHours(48), 10);
        verify(candidatePort).claimPendingStockRewardIds(Duration.ofHours(48), 10);
    }

    @Test
    void runReconcile_clampsOversizedBatchSizeToMax() {
        when(candidatePort.findMissingRewardCandidates(any(), eq(100))).thenReturn(List.of());
        when(candidatePort.claimPendingStockRewardIds(any(), eq(100))).thenReturn(List.of());

        service.runReconcile(RewardReconcileService.ReconcileRequest.admin(500, false, UUID.randomUUID()));

        verify(candidatePort).findMissingRewardCandidates(Duration.ofHours(48), 100);
    }

    @Test
    void runReconcile_releasesLockWhenCandidateQueryFails() {
        when(candidatePort.findMissingRewardCandidates(any(), anyInt()))
                .thenThrow(new RuntimeException("db down"));

        assertThrows(RuntimeException.class, () -> service.runReconcile());

        verify(lockPort).release();
        verify(rewardEvaluationService, never()).handleStampCollected(any(), any());
    }

    @Test
    void runReconcile_adminRequestReturnsInitiatedByAdminId() {
        UUID adminId = UUID.randomUUID();
        when(candidatePort.findMissingRewardCandidates(any(), anyInt())).thenReturn(List.of());
        when(candidatePort.claimPendingStockRewardIds(any(), anyInt())).thenReturn(List.of());

        var result = service.runReconcile(RewardReconcileService.ReconcileRequest.admin(5, false, adminId));

        assertFalse(result.skipped());
        assertEquals(adminId, result.initiatedByAdminId());
    }

    @Test
    void runReconcile_stopsWhenMaxDurationExceeded() {
        RewardProperties properties = new RewardProperties();
        properties.setReconcileLookback(Duration.ofHours(48));
        properties.setReconcileBatchSize(10);
        properties.setReconcileMaxDuration(Duration.ofMillis(40));
        properties.setReconcileMaxBatches(5);
        properties.setReconcileMaxBatchSize(100);
        service = new RewardReconcileService(
                candidatePort, lockPort, rewardEvaluationService, properties, new SimpleMeterRegistry());

        List<RewardReconcileCandidatePort.UserCampaignPair> many = new ArrayList<>();
        UUID campaignId = UUID.randomUUID();
        for (int i = 0; i < 20; i++) {
            many.add(new RewardReconcileCandidatePort.UserCampaignPair(UUID.randomUUID(), campaignId));
        }
        when(candidatePort.findMissingRewardCandidates(any(), anyInt())).thenReturn(many).thenReturn(List.of());
        lenient().when(candidatePort.claimPendingStockRewardIds(any(), anyInt())).thenReturn(List.of());
        doAnswer(inv -> {
            Thread.sleep(25);
            return null;
        }).when(rewardEvaluationService).handleStampCollected(any(), any());

        var result = service.runReconcile();

        assertTrue(result.missingExamined() >= 1);
        assertTrue(result.missingExamined() < many.size());
    }
}
