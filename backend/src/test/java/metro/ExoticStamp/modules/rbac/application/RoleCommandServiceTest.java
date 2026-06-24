package metro.ExoticStamp.modules.rbac.application;

import metro.ExoticStamp.config.RbacProperties;
import metro.ExoticStamp.modules.auth.application.AuditLogService;
import metro.ExoticStamp.modules.rbac.application.command.AssignRoleCommand;
import metro.ExoticStamp.modules.rbac.application.command.RevokeRoleCommand;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleAlreadyAssignedException;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import metro.ExoticStamp.modules.rbac.domain.exception.LastAdminProtectionException;
import metro.ExoticStamp.modules.rbac.domain.model.Role;
import metro.ExoticStamp.modules.rbac.domain.model.RoleStatus;
import metro.ExoticStamp.modules.rbac.domain.repository.RoleRepository;
import metro.ExoticStamp.modules.rbac.domain.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleCommandServiceTest {

    private static final UUID USER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ADMIN_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RbacProperties rbacProperties;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private RbacSecurityContextHelper securityContextHelper;

    @InjectMocks
    private RoleCommandService roleCommandService;

    private static final UUID USER_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @Test
    void assignRole_success() {
        when(rbacProperties.getMaxRoleCodeLength()).thenReturn(64);
        Role role = Role.builder()
                .id(USER_ROLE_ID)
                .role("USER")
                .status(RoleStatus.ACTIVE)
                .systemRole(false)
                .build();
        when(roleRepository.findByRoleCode("USER")).thenReturn(Optional.of(role));
        when(userRoleRepository.existsByUserIdAndRoleId(USER, USER_ROLE_ID)).thenReturn(false);

        roleCommandService.assignRole(AssignRoleCommand.builder().userId(USER).roleName("USER").build());

        verify(userRoleRepository).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void assignRole_duplicate_throws() {
        when(rbacProperties.getMaxRoleCodeLength()).thenReturn(64);
        Role role = Role.builder()
                .id(USER_ROLE_ID)
                .role("USER")
                .status(RoleStatus.ACTIVE)
                .build();
        when(roleRepository.findByRoleCode("USER")).thenReturn(Optional.of(role));
        when(userRoleRepository.existsByUserIdAndRoleId(USER, USER_ROLE_ID)).thenReturn(true);

        assertThrows(RoleAlreadyAssignedException.class, () -> roleCommandService.assignRole(
                AssignRoleCommand.builder().userId(USER).roleName("USER").build()));
    }

    @Test
    void revokeRole_whenNotAssigned_isNoop() {
        when(rbacProperties.getMaxRoleCodeLength()).thenReturn(64);
        Role role = Role.builder().id(USER_ROLE_ID).role("USER").status(RoleStatus.ACTIVE).build();
        when(roleRepository.findByRoleCode("USER")).thenReturn(Optional.of(role));
        when(userRoleRepository.existsByUserIdAndRoleId(USER, USER_ROLE_ID)).thenReturn(false);

        assertDoesNotThrow(() -> roleCommandService.revokeRole(
                RevokeRoleCommand.builder().userId(USER).roleName("USER").build()));
    }

    @Test
    void concurrentAssignRole_duplicateThrows() throws Exception {
        when(rbacProperties.getMaxRoleCodeLength()).thenReturn(64);
        Role role = Role.builder().id(USER_ROLE_ID).role("USER").status(RoleStatus.ACTIVE).build();
        when(roleRepository.findByRoleCode("USER")).thenReturn(Optional.of(role));
        AtomicInteger exists = new AtomicInteger();
        when(userRoleRepository.existsByUserIdAndRoleId(USER, USER_ROLE_ID))
                .thenAnswer(inv -> exists.getAndIncrement() > 0);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger conflicts = new AtomicInteger();
        Runnable task = () -> {
            try {
                start.await();
                roleCommandService.assignRole(AssignRoleCommand.builder().userId(USER).roleName("USER").build());
            } catch (RoleAlreadyAssignedException e) {
                conflicts.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        pool.submit(task);
        pool.submit(task);
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS));
        assertEquals(1, conflicts.get());
    }

    @Test
    void revokeRole_whenLastActiveAdmin_throws() {
        when(rbacProperties.getAdminRoleCode()).thenReturn("ADMIN");
        when(rbacProperties.getMaxRoleCodeLength()).thenReturn(64);
        Role admin = Role.builder()
                .id(ADMIN_ROLE_ID)
                .role("ADMIN")
                .status(RoleStatus.ACTIVE)
                .systemRole(true)
                .build();
        when(roleRepository.findByRoleCode("ADMIN")).thenReturn(Optional.of(admin));
        when(userRoleRepository.existsByUserIdAndRoleId(USER, ADMIN_ROLE_ID)).thenReturn(true);
        when(userRoleRepository.countActiveUsersWithRoleCode("ADMIN")).thenReturn(1L);

        assertThrows(LastAdminProtectionException.class, () -> roleCommandService.revokeRole(
                RevokeRoleCommand.builder().userId(USER).roleName("ADMIN").build()));
    }
}
