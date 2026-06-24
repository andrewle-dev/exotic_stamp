# IMPLEMENTATION GUARDRAILS - EXOTIC STAMP

> Dùng như reference checklist khi code từng file, để đảm bảo dependencies đúng, pattern consistent.

---

## I. FILE STRUCTURE TEMPLATE (mỗi module)

```
src/main/java/metro/ExoticStamp/modules/{moduleName}/
├── domain/
│   ├── entity/
│   │   ├── {Entity}Entity.java         (@Entity, JPA mapping)
│   │   ├── {Enum}.java                 (state/type enum)
│   │   └── {ValueObject}.java          (optional, immutable)
│   ├── repository/
│   │   └── {Entity}Repository.java     (❌ INTERFACE ONLY, no impl)
│   ├── service/
│   │   └── {Module}DomainService.java  (business rule validation)
│   ├── exception/
│   │   ├── {Entity}NotFoundException.java
│   │   ├── {Reason}Exception.java
│   │   └── {Validation}Exception.java
│   └── event/
│       └── {Event}DomainEvent.java     (optional)
│
├── application/
│   ├── command/
│   │   ├── {Action}{Entity}Command.java         (input DTO for command)
│   │   └── {Action}{Entity}CommandHandler.java  (optional, if using handler pattern)
│   ├── query/
│   │   └── {Get/Search}{Entity}Query.java       (input for query)
│   ├── service/
│   │   ├── {Entity}CommandService.java          (@Transactional write)
│   │   └── {Entity}QueryService.java            (@Transactional(readOnly=true) read)
│   ├── mapper/
│   │   └── {Entity}AppMapper.java               (MapStruct or manual)
│   └── port/
│       └── {Service}Port.java                   (outbound interface)
│
├── infrastructure/
│   ├── repository/
│   │   ├── Jpa{Entity}Repository.java           (extends JpaRepository)
│   │   └── {Entity}RepositoryAdapter.java       (implements domain interface)
│   ├── cache/
│   │   └── {Entity}CacheRepository.java         (extends BaseCacheRepository)
│   ├── persistence/
│   │   └── {Service}PersistenceAdapter.java     (for external storage)
│   ├── event/
│   │   └── {Event}Listener.java                 (@EventListener, @Async)
│   └── integration/
│       └── {External}Adapter.java               (3rd party integration)
│
└── presentation/
    ├── controller/
    │   └── {Entity}Controller.java              (@RestController)
    ├── request/
    │   ├── {Action}{Entity}Request.java         (@Valid DTO input)
    │   └── {Query}{Entity}Request.java
    ├── response/
    │   ├── {Entity}Response.java                (DTO output)
    │   └── {Entity}ListResponse.java
    └── exception/
        └── {Entity}ControllerExceptionHandler.java (optional per-module)
```

---

## II. LAYER DEPENDENCY RULES (Critical!)

### ✅ CORRECT DIRECTIONS

```
presentation/controller 
    ↓ imports
application/service (CommandService, QueryService)
    ↓ imports
application/command (Command objects)
    ↓ imports
domain/entity
domain/repository (interface)
domain/service
    ↓ imports
domain/exception
domain/event

infrastructure/repository (adapter)
    ↓ imports
domain/repository (interface)
infrastructure/persistence (JPA)
    ↓ imports
(NO RESTRICTION - can import anything below)
```

### ❌ FORBIDDEN DIRECTIONS

```
❌ domain → presentation
❌ domain → infrastructure  
❌ application → infrastructure.persistence (JpaRepository directly)
❌ presentation → infrastructure
❌ presentation → domain
```

### ✅ ALLOWED (within same layer)

```
✅ application.service → application.mapper
✅ application.service → application.command
✅ infrastructure.repository → infrastructure.cache
✅ infrastructure.event → application.service (async call)
✅ presentation.controller → presentation.request
```

---

## III. ENTITY ANNOTATION CHECKLIST

```java
@Entity
@Table(name = "table_name", indexes = {
    @Index(name = "idx_...", columnList = "col1, col2")
})
public class MyEntity extends BaseEntity {
    
    // ✅ DO:
    @Column(nullable = false, unique = true)
    private String field1;
    
    @Column(length = 255)
    @Enumerated(EnumType.STRING)
    private MyEnum status;
    
    @Version  // optimistic lock
    private Long version;
    
    // ✅ OK (lazy load for large fields):
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String largeData;
    
    // ❌ DON'T:
    @Transient  // ❌ if avoidable
    private String computed;  // move to service instead
    
    // ❌ DON'T do business logic in entity:
    public void collect() { ... }  // ❌ move to DomainService
}
```

---

## IV. REPOSITORY PATTERN (domain interface + adapter)

### Domain Interface (NO IMPL)

```java
// modules/metro/domain/repository/StationRepository.java
public interface StationRepository {
    Optional<Station> findById(UUID id);
    Optional<Station> findByNfcTagId(String nfcTag);
    Optional<Station> findByQrCodeToken(String qrToken);
    List<Station> findByLineId(UUID lineId);
    Station save(Station station);
    void delete(UUID id);
}
```

### JPA Repository

```java
// modules/metro/infrastructure/repository/JpaStationRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface JpaStationRepository extends JpaRepository<StationEntity, UUID> {
    Optional<StationEntity> findByNfcTagId(String nfcTag);
    Optional<StationEntity> findByQrCodeToken(String qrToken);
    List<StationEntity> findByLineId(UUID lineId);
}
```

### Adapter (bridges domain interface → JPA impl)

```java
// modules/metro/infrastructure/repository/StationRepositoryAdapter.java
@Component
@RequiredArgsConstructor
public class StationRepositoryAdapter implements StationRepository {
    
    private final JpaStationRepository jpaRepository;
    private final StationMapper mapper;  // entity ↔ domain mapping
    
    @Override
    public Optional<Station> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);  // convert JPA entity → domain
    }
    
    @Override
    public Optional<Station> findByNfcTagId(String nfcTag) {
        return jpaRepository.findByNfcTagId(nfcTag)
            .map(mapper::toDomain);
    }
    
    @Override
    public Station save(Station station) {
        StationEntity entity = mapper.toEntity(station);  // domain → JPA entity
        StationEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
```

---

## V. SERVICE PATTERN (Command & Query)

### CommandService (@Transactional write)

```java
// modules/collection/application/service/CollectionCommandService.java
@Service
@RequiredArgsConstructor
@Transactional  // ✅ write operations need transaction
public class CollectionCommandService {
    
    private final UserStampRepository stampRepository;  // domain interface
    private final StationRepository stationRepository;
    private final CampaignRepository campaignRepository;
    private final CollectionDomainService domainService;
    private final UserStampMapper mapper;
    private final UserStampCacheRepository cacheRepo;  // for eviction
    private final ApplicationEventPublisher eventPublisher;
    
    public StampCollectResponse collectStamp(CollectStampCommand cmd) {
        // 1. Domain validation (throws exception if violated)
        Station station = stationRepository.findById(cmd.stationId())
            .orElseThrow(() -> new StationNotFoundException(cmd.stationId()));
        
        Campaign campaign = campaignRepository.findById(cmd.campaignId())
            .orElseThrow(() -> new CampaignNotFoundException(cmd.campaignId()));
        
        domainService.validateStampCollection(
            cmd.userId(), 
            station.getId(), 
            campaign.getId()
        );  // throws StampAlreadyCollectedException if duplicate
        
        // 2. Business logic
        UserStamp stamp = UserStamp.create(
            cmd.userId(),
            station,
            campaign,
            cmd.deviceFingerprint(),
            cmd.gpsLat(),
            cmd.gpsLng()
        );
        
        // 3. Persist
        UserStamp savedStamp = stampRepository.save(stamp);
        
        // 4. Cache eviction (important!)
        cacheRepo.evict("user-stamps:" + cmd.userId());
        
        // 5. Fire event (async reward evaluation)
        eventPublisher.publishEvent(new PostCollectStampEvent(savedStamp));
        
        // 6. Return response
        return mapper.toResponse(savedStamp);
    }
}
```

### QueryService (@Transactional(readOnly=true))

```java
// modules/collection/application/service/CollectionQueryService.java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // ✅ read-only for performance
public class CollectionQueryService {
    
    private final UserStampRepository stampRepository;
    private final UserStampCacheRepository cacheRepo;
    private final UserStampMapper mapper;
    
    public StampBookResponse getStampBook(UUID userId, UUID lineId) {
        // 1. Try cache first
        String cacheKey = "stamp-book:" + userId + ":" + lineId;
        StampBookResponse cached = cacheRepo.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 2. Query database if cache miss
        List<UserStamp> stamps = stampRepository.findByUserIdAndLineId(userId, lineId);
        
        // 3. Build response (aggregate data)
        StampBookResponse response = StampBookResponse.builder()
            .userId(userId)
            .lineId(lineId)
            .totalStations(20)  // or query
            .collectedCount(stamps.size())
            .stamps(mapper.toResponseList(stamps))
            .build();
        
        // 4. Cache result (TTL from config)
        cacheRepo.set(cacheKey, response, Duration.ofMinutes(30));
        
        return response;
    }
}
```

---

## VI. MAPPER PATTERN

### MapStruct (recommended)

```java
// modules/metro/application/mapper/StationAppMapper.java
@Mapper(componentModel = "spring")
public interface StationAppMapper {
    
    // Domain ↔ Response DTO
    StationResponse toResponse(Station station);
    
    List<StationResponse> toResponseList(List<Station> stations);
    
    // Request DTO → Domain
    Station toDomain(CreateStationRequest request);
    
    // JPA Entity ↔ Domain (for adapter)
    Station toDomain(StationEntity entity);
    
    StationEntity toEntity(Station station);
}
```

### Manual Mapper

```java
@Component
@RequiredArgsConstructor
public class UserStampAppMapper {
    
    public UserStampResponse toResponse(UserStamp stamp) {
        return UserStampResponse.builder()
            .id(stamp.getId())
            .stationId(stamp.getStation().getId())
            .stationName(stamp.getStation().getName())
            .collectedAt(stamp.getCollectedAt())
            .stampDesignUrl(stamp.getStampDesign().getImageUrl())
            .build();
    }
    
    public List<UserStampResponse> toResponseList(List<UserStamp> stamps) {
        return stamps.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}
```

---

## VII. DOMAIN SERVICE PATTERN

```java
// modules/collection/domain/service/CollectionDomainService.java
@Service
@RequiredArgsConstructor
public class CollectionDomainService {
    
    private final UserStampRepository stampRepository;
    private final CampaignRepository campaignRepository;
    
    /**
     * Validate stamp collection constraints.
     * 
     * @throws StampAlreadyCollectedException if (user, station, campaign) exists
     * @throws CampaignExpiredException if campaign not active
     */
    public void validateStampCollection(UUID userId, UUID stationId, UUID campaignId) {
        // Check unique constraint at DB level, but also domain-level validation
        boolean exists = stampRepository.existsByUserIdAndStationIdAndCampaignId(
            userId, stationId, campaignId
        );
        
        if (exists) {
            throw new StampAlreadyCollectedException(userId, stationId);
        }
        
        // Check campaign active
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new CampaignNotFoundException(campaignId));
        
        if (!campaign.isActive()) {
            throw new CampaignExpiredException(campaignId);
        }
    }
    
    /**
     * Evaluate if stamp collection hits milestone.
     */
    public Optional<Milestone> evaluateMilestone(UUID userId, UUID campaignId) {
        long stampCount = stampRepository.countByUserIdAndCampaignId(userId, campaignId);
        // ... find milestone matching stampCount
        return Optional.empty();  // or return matching milestone
    }
}
```

---

## VIII. CONTROLLER PATTERN

```java
// modules/collection/presentation/controller/CollectionController.java
@RestController
@RequestMapping("/api/v1/collection")
@RequiredArgsConstructor
@Tag(name = "Collection", description = "Stamp collection operations")
public class CollectionController {
    
    private final CollectionCommandService commandService;
    private final CollectionQueryService queryService;
    private final UserStampMapper mapper;
    
    @PostMapping("/collect")
    @Operation(summary = "Collect stamp by station scan")
    @ApiResponse(responseCode = "201", description = "Stamp collected")
    @ApiResponse(responseCode = "409", description = "Stamp already collected")
    public ResponseEntity<ApiResponse<StampCollectResponse>> collectStamp(
        @Valid @RequestBody CollectStampRequest request,
        @AuthenticationPrincipal UserPrincipal user
    ) {
        CollectStampCommand cmd = CollectStampCommand.builder()
            .userId(user.getId())
            .stationId(request.stationId())
            .campaignId(request.campaignId())
            .deviceFingerprint(request.deviceFingerprint())
            .gpsLat(request.latitude())
            .gpsLng(request.longitude())
            .build();
        
        StampCollectResponse response = commandService.collectStamp(cmd);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Stamp collected successfully"));
    }
    
    @GetMapping("/stamp-book/{userId}/{lineId}")
    @Operation(summary = "Get user's stamp book for a line")
    public ResponseEntity<ApiResponse<StampBookResponse>> getStampBook(
        @PathVariable UUID userId,
        @PathVariable UUID lineId
    ) {
        StampBookResponse response = queryService.getStampBook(userId, lineId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

---

## IX. REQUEST/RESPONSE DTO PATTERN

### Request DTO (with validation)

```java
// modules/collection/presentation/request/CollectStampRequest.java
@Data
@Builder
public class CollectStampRequest {
    
    @NotNull(message = "stationId cannot be null")
    private UUID stationId;
    
    @NotNull(message = "campaignId cannot be null")
    private UUID campaignId;
    
    @NotBlank(message = "deviceFingerprint cannot be blank")
    private String deviceFingerprint;
    
    @NotNull(message = "latitude is required")
    @DecimalMin(value = "-90.0", message = "latitude must be between -90 and 90")
    @DecimalMax(value = "90.0")
    private Double latitude;
    
    @NotNull(message = "longitude is required")
    @DecimalMin(value = "-180.0", message = "longitude must be between -180 and 180")
    @DecimalMax(value = "180.0")
    private Double longitude;
}
```

### Response DTO (no business logic)

```java
// modules/collection/presentation/response/StampCollectResponse.java
@Data
@Builder
public class StampCollectResponse {
    
    private UUID stampId;
    private StationInfo station;
    private StampDesignInfo design;
    private LocalDateTime collectedAt;
    
    // Milestone info (if achieved)
    private MilestoneInfo milestone;
    private RewardInfo reward;
    
    // Progress info
    private Integer totalStationsInLine;
    private Integer collectedInLine;
    
    @Data
    @Builder
    public static class StationInfo {
        private UUID id;
        private String name;
        private String lineName;
    }
    
    @Data
    @Builder
    public static class StampDesignInfo {
        private UUID id;
        private String imageUrl;
        private String description;
    }
    
    @Data
    @Builder
    public static class MilestoneInfo {
        private UUID id;
        private Integer requiredStamps;
    }
    
    @Data
    @Builder
    public static class RewardInfo {
        private UUID id;
        private String name;
        private String rewardType;
    }
}
```

---

## X. EXCEPTION HANDLING

### Domain Exceptions

```java
// modules/collection/domain/exception/StampAlreadyCollectedException.java
public class StampAlreadyCollectedException extends DomainException {
    
    public StampAlreadyCollectedException(UUID userId, UUID stationId) {
        super("Stamp already collected by user " + userId + " at station " + stationId);
        this.errorCode = "STAMP_DUPLICATE";
        this.httpStatus = HttpStatus.CONFLICT;
    }
}

// modules/common/exceptions/DomainException.java
public abstract class DomainException extends RuntimeException {
    protected String errorCode;
    protected HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    
    public DomainException(String message) {
        super(message);
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
```

### Global Handler (existing, just use it)

```java
// common/exceptions/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
        DomainException ex,
        HttpServletRequest request
    ) {
        return ResponseEntity
            .status(ex.getHttpStatus())
            .body(ErrorResponse.of(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getHttpStatus().value(),
                request.getRequestURI()
            ));
    }
}
```

---

## XI. CACHING PATTERN (Redis)

### Using BaseCacheRepository

```java
// modules/collection/infrastructure/cache/UserStampCacheRepository.java
@Component
@RequiredArgsConstructor
public class UserStampCacheRepository extends BaseCacheRepository<StampBookResponse> {
    
    private static final String CACHE_PREFIX = "stamp-book:";
    
    @Value("${cache.ttl.user-data:1800}")  // 30 minutes default
    private long ttl;
    
    public StampBookResponse get(String key) {
        return super.get(CACHE_PREFIX + key, StampBookResponse.class);
    }
    
    public void set(String key, StampBookResponse value) {
        super.set(CACHE_PREFIX + key, value, Duration.ofSeconds(ttl));
    }
    
    public void evict(String key) {
        super.delete(CACHE_PREFIX + key);
    }
}
```

---

## XII. EVENT-DRIVEN PATTERN

### Domain Event

```java
// modules/collection/domain/event/PostCollectStampEvent.java
@Getter
public class PostCollectStampEvent extends ApplicationEvent {
    
    private final UserStamp stamp;
    private final LocalDateTime occurredAt;
    
    public PostCollectStampEvent(UserStamp stamp) {
        super(stamp);
        this.stamp = stamp;
        this.occurredAt = LocalDateTime.now();
    }
}
```

### Event Listener (async, in reward module)

```java
// modules/reward/infrastructure/event/PostCollectStampEventListener.java
@Component
@RequiredArgsConstructor
public class PostCollectStampEventListener {
    
    private final RewardCommandService rewardService;
    
    @EventListener
    @Async  // ✅ non-blocking
    public void onStampCollected(PostCollectStampEvent event) {
        try {
            rewardService.evaluateAndIssueReward(
                event.getStamp().getUserId(),
                event.getStamp().getCampaignId()
            );
        } catch (Exception e) {
            log.error("Failed to evaluate reward for user {}", event.getStamp().getUserId(), e);
            // Implement retry logic if needed
        }
    }
}
```

---

## XIII. TRANSACTION BOUNDARIES

### ✅ DO

```java
@Service
@Transactional
public class MyCommandService {
    
    // Method 1: write operation = @Transactional
    public Result doWrite(Command cmd) {
        repo.save(entity);  // single transaction
    }
    
    // Method 2: call domain service = same transaction
    public Result doComplexWrite(Command cmd) {
        domainService.validate(entity);  // same txn
        repo.save(entity);                 // same txn
    }
    
    // Method 3: event publish = still same txn
    public Result doWriteWithEvent(Command cmd) {
        repo.save(entity);
        eventPublisher.publishEvent(event);  // queued, sent after commit
        return result;
    }
}
```

### ❌ DON'T

```java
// ❌ calling write service from read service
@Service
@Transactional(readOnly = true)
public class MyQueryService {
    
    private final MyCommandService cmdService;  // ❌ DON'T INJECT
    
    public void query() {
        cmdService.doWrite(cmd);  // ❌ mixes read + write transaction!
    }
}
```

---

## XIV. TESTING CHECKLIST

### Unit Test Template

```java
class CollectionCommandServiceTest {
    
    @Mock private UserStampRepository stampRepo;
    @Mock private StationRepository stationRepo;
    @Mock private CollectionDomainService domainService;
    @InjectMocks private CollectionCommandService service;
    
    @Test
    void collectStamp_success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        CollectStampCommand cmd = CollectStampCommand.builder()
            .userId(userId)
            .stationId(stationId)
            .build();
        
        Station mockStation = new Station(...);
        when(stationRepo.findById(stationId)).thenReturn(Optional.of(mockStation));
        when(domainService.validateStampCollection(...)).thenNothing();
        
        UserStamp mockStamp = new UserStamp(...);
        when(stampRepo.save(any())).thenReturn(mockStamp);
        
        // Act
        StampCollectResponse response = service.collectStamp(cmd);
        
        // Assert
        assertNotNull(response.stampId);
        verify(stampRepo).save(any(UserStamp.class));
        verify(domainService).validateStampCollection(userId, stationId, null);
    }
    
    @Test
    void collectStamp_duplicate_throwsException() {
        // Arrange
        when(domainService.validateStampCollection(...))
            .thenThrow(StampAlreadyCollectedException.class);
        
        // Act & Assert
        assertThrows(StampAlreadyCollectedException.class, () -> {
            service.collectStamp(cmd);
        });
    }
}
```

### Integration Test Template

```java
@SpringBootTest
class CollectionIntegrationTest {
    
    @Autowired private CollectionCommandService commandService;
    @Autowired private UserStampRepository stampRepository;
    @Autowired private TestEntityManager em;
    
    @Test
    @Transactional
    void collectStamp_savesToDatabase() {
        // Arrange
        StationEntity station = em.persistAndFlush(new StationEntity(...));
        
        // Act
        StampCollectResponse response = commandService.collectStamp(cmd);
        em.flush();
        
        // Assert
        UserStampEntity saved = em.find(UserStampEntity.class, response.stampId);
        assertNotNull(saved);
        assertEquals(station.getId(), saved.getStationId());
    }
}
```

---

## XV. QUICK REFERENCE - COMMON MISTAKES

| Mistake | ❌ Wrong | ✅ Right |
|---------|---------|---------|
| JPA in application | `@Autowired JpaRepository` | `@Autowired DomainRepository` |
| No @Transactional on write | `public void save() { repo.save() }` | `@Transactional public void save()` |
| readOnly=true on write | `@Transactional(readOnly=true) save()` | `@Transactional save()` |
| Business logic in entity | `entity.validate()` | `domainService.validate(entity)` |
| Exposing sensitive data | `new UserResponse(user.password)` | mapper filters sensitive fields |
| No cache eviction | After write, query returns stale data | `cacheRepo.evict(key)` after write |
| Hardcoded constants | `TTL = 1800` in code | `@Value("${cache.ttl}")` |
| No domain validation | Rely only on DB constraint | Add domain service validation |
| Direct event publishing | `new Event()` without context | `eventPublisher.publishEvent(event)` |
| Synchronous event | `listener.onEvent()` blocking | `@EventListener @Async` non-blocking |

---

## XVI. CODE REVIEW CHECKLIST

Before pushing, check:

- [ ] No layer imports violate dependency direction
- [ ] All write services have `@Transactional`
- [ ] All read services have `@Transactional(readOnly=true)`
- [ ] Domain service does business validation
- [ ] Repository is interface in domain, adapter in infrastructure
- [ ] DTOs don't expose sensitive fields (passwords, tokens, etc.)
- [ ] Cache eviction after writes (where applicable)
- [ ] Events are async (@Async listener)
- [ ] Exceptions are domain-level (not JPA exceptions)
- [ ] Tests cover happy path + error path
- [ ] No hardcoded TTL/rule values (use @Value)
- [ ] Hot-path queries are indexed (check DDL)
- [ ] Mapper properly converts domain ↔ DTO
- [ ] API documentation clear (OpenAPI tags, @Operation)

---

Done! Use this as reference while coding. When you're ready to start, we'll go line-by-line through Metro module implementation.
