# CLAUDE.md - BMW M Club Korea API Server

This file provides guidance for AI assistants working with this codebase.

## Project Overview

**mck-api-server** is the backend API server for BMW M Club Korea community website. It uses a **Modular Monolith** architecture with Spring Modulith, combining the benefits of microservices (domain isolation) with the simplicity of a monolith (single deployment unit).

### Tech Stack

| Category | Technology | Version |
|----------|------------|---------|
| Language | Java | 21 (LTS) |
| Framework | Spring Boot | 4.0.1 |
| Module System | Spring Modulith | 2.0.1 |
| Database | PostgreSQL | 16+ |
| Migration | Flyway | - |
| Build Tool | Gradle | 8.x |
| Auth | OAuth2 + Passkey + JWT | - |
| Real-time | gRPC Streaming | - |
| File Storage | DigitalOcean Spaces | S3 Compatible |
| Scheduling | Quartz | - |

## Quick Commands

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run the application (requires PostgreSQL)
./gradlew bootRun

# Start PostgreSQL with Docker Compose
docker compose up -d

# Clean build
./gradlew clean build

# Check dependencies
./gradlew dependencies

# Generate proto files (gRPC)
./gradlew generateProto
```

## Project Structure

```
mck-api-server/
├── src/main/java/kr/mclub/apiserver/
│   ├── MckApiServerApplication.java    # Main entry point
│   ├── shared/                         # Shared Kernel (all modules depend on this)
│   │   ├── domain/                     # BaseEntity, DomainEvent
│   │   ├── exception/                  # GlobalExceptionHandler, ErrorCode
│   │   ├── security/                   # SecurityConfig, JwtTokenProvider
│   │   └── util/                       # ApiResponse, PageResponse
│   ├── user/                           # User Module
│   ├── membership/                     # Membership Module (정회원 가입)
│   ├── landing/                        # Landing Page Module
│   ├── community/                      # Community Module (게시판)
│   ├── admin/                          # Admin Module
│   ├── chat/                           # Chat Module (gRPC)
│   ├── navercafe/                      # Naver Cafe Integration
│   └── notification/                   # Notification Module
├── src/main/resources/
│   ├── application.properties          # App configuration
│   └── db/migration/                   # Flyway migrations (V1__, V2__, ...)
├── src/test/java/
├── docs/                               # Project documentation
│   ├── ARCHITECTURE.md                 # Architecture design (detailed)
│   ├── API_SPECIFICATION.md            # REST API specification
│   ├── DATABASE_SCHEMA.md              # Database schema design
│   ├── DESIGN_PLAN.md                  # Design overview
│   └── IMPLEMENTATION_TODO.md          # Implementation checklist
├── build.gradle                        # Gradle build configuration
├── compose.yaml                        # Docker Compose for PostgreSQL
└── settings.gradle                     # Project settings
```

## Architecture

### Module Structure

Each module follows a standard package structure:

```
kr.mclub.apiserver.{module}/
├── package-info.java              # Spring Modulith module config
├── domain/                        # JPA entities, value objects, enums
├── repository/                    # JPA repositories
├── service/                       # Business logic
├── api/                           # REST controllers
│   └── dto/                       # Request/Response DTOs
├── event/                         # Domain events and listeners
├── grpc/                          # gRPC services (chat module only)
├── webhook/                       # Webhook handlers (if needed)
└── scheduler/                     # Scheduled tasks (if needed)
```

### Module Dependencies

**Important Rule**: Modules communicate via **events only**, not direct dependencies.

```
                    ┌─────────────┐
                    │   SHARED    │  (BaseEntity, Security, Utils)
                    └──────┬──────┘
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
    ┌──────────┐    ┌────────────┐    ┌──────────┐
    │   USER   │◄───│ MEMBERSHIP │    │ LANDING  │
    └────┬─────┘    └─────┬──────┘    └────┬─────┘
         │                │                │
         │    Events      │    Events      │
         ▼                ▼                ▼
    ┌──────────────────────────────────────────┐
    │              COMMUNITY                    │
    └──────────────────────────────────────────┘
         │                │                │
         ▼                ▼                ▼
    ┌────────┐    ┌────────────┐    ┌──────────┐
    │  CHAT  │    │ NAVERCAFE  │    │  ADMIN   │
    └────────┘    └────────────┘    └──────────┘
                         │
                         ▼
                  ┌────────────┐
                  │NOTIFICATION│  (Subscribes to all events)
                  └────────────┘
```

### Event-Driven Communication

Modules publish and subscribe to domain events:

```java
// Publishing an event
@Service
@RequiredArgsConstructor
public class UserEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publishUserRegistered(User user) {
        eventPublisher.publishEvent(new UserRegisteredEvent(
            user.getId(), user.getEmail(), user.getGrade()));
    }
}

// Subscribing to an event
@Component
@RequiredArgsConstructor
public class ChatEventListener {
    @ApplicationModuleListener
    public void onEventCreated(EventCreatedEvent event) {
        // Create chat room for the event
    }
}
```

## Key Domain Concepts

### User Grade System (등급 체계)

User grades are stored in the `user_grades` **database table** (not an enum) for dynamic management:

| Grade | Code | Permission Level | Notes |
|-------|------|------------------|-------|
| Developer | `DEVELOPER` | 10 | System grade (cannot delete) |
| Advisor | `ADVISOR` | 9 | 고문 - Former presidents |
| President | `PRESIDENT` | 8 | 회장 |
| Vice President | `VICE_PRESIDENT` | 7 | 부회장 |
| Director | `DIRECTOR` | 6 | 이사 - Has assigned part |
| Regular Member | `REGULAR` | 5 | 정회원 - Verified + paid |
| Associate Member | `ASSOCIATE` | 3 | 준회원 - System grade |
| Partner | `PARTNER` | 2 | 파트너사 |

### Membership Flow

```
OAuth Login → Associate (PENDING)
    ↓
Submit Application + Documents
    ↓
OCR Verification → Admin Approval
    ↓
Payment (Enrollment 200K + Annual 200K = 400K KRW)
    ↓
Regular Member (REGULAR)
    ↓
Annual Renewal (every January)
```

### Permission Groups (게시판 권한)

Board permissions are managed through **permission groups**:

- `READ`, `WRITE`, `MOVE`, `COMMENT`, `DELETE`, `HARD_DELETE`, `SHARE`
- Groups can be assigned to users and mapped to boards
- Executives can dynamically create/modify permission groups

## Coding Conventions

### General

- Use **Korean comments** for domain-specific terms (회원, 정회원, 게시판)
- Use **English** for code, variable names, and technical comments
- All entities extend `BaseEntity` or `BaseTimeEntity`
- Use `@RequiredArgsConstructor` for dependency injection
- Use Java Records for DTOs and Events

### Entity Pattern

```java
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer memberNumber;  // 정회원 번호

    @Column(nullable = false)
    private String realName;  // 실명

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id", nullable = false)
    private UserGrade grade;

    // Business methods...
    public String getDisplayName() {
        // Returns "610 홍길동" or "610 홍길동 (이사)" etc.
    }
}
```

### Service Pattern

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserEventPublisher eventPublisher;

    @Transactional
    public void changeGrade(Long userId, Long newGradeId, Long adminId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.changeGrade(newGradeId);
        eventPublisher.publishGradeChanged(user, adminId);
    }
}
```

### API Response Pattern

```java
// Standard success response
{
  "success": true,
  "data": { ... },
  "message": "Success",
  "timestamp": "2025-12-30T12:00:00Z"
}

// Error response
{
  "success": false,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "사용자를 찾을 수 없습니다."
  },
  "timestamp": "2025-12-30T12:00:00Z"
}
```

### Controller Pattern

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile(@CurrentUser Long userId) {
        return ApiResponse.success(userService.getProfile(userId));
    }
}
```

## Database

### Local Development

```bash
# Start PostgreSQL
docker compose up -d

# Connection details (from compose.yaml)
Host: localhost
Port: 5432
Database: mydatabase
Username: myuser
Password: secret
```

### Flyway Migrations

- Migration files: `src/main/resources/db/migration/V{version}__{description}.sql`
- Naming: `V1__create_user_module_tables.sql`, `V2__create_membership_module_tables.sql`, etc.
- Always create new migration files, never edit existing ones

### Key Tables

| Module | Tables |
|--------|--------|
| User | `user_grades`, `users`, `oauth_accounts`, `passkey_credentials`, `member_vehicles` |
| Membership | `membership_applications`, `application_documents`, `ocr_results`, `payment_records`, `membership_periods`, `director_parts`, `annual_fee_configs` |
| Community | `permission_groups`, `board_permission_mappings`, `user_permission_groups`, `boards`, `posts`, `comments`, `attachments` |
| Chat | `chat_rooms`, `chat_participants`, `chat_messages` |

## Testing

```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew test --tests "kr.mclub.apiserver.user.*"

# Run with coverage
./gradlew test jacocoTestReport
```

### Test Structure

```java
// Unit test
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUserById() { ... }
}

// Integration test
@SpringBootTest
@Transactional
class UserModuleIntegrationTest { ... }

// Spring Modulith structure test
@SpringBootTest
class ModularityTests {
    @Test
    void verifyModuleStructure() {
        ApplicationModules.of(MckApiServerApplication.class).verify();
    }
}
```

## CI/CD

### GitHub Actions

- **claude.yml**: Responds to `@claude` mentions in issues/PRs
- **claude-code-review.yml**: Automatic PR code review

### Workflow Triggers

```yaml
# claude.yml - Interactive assistance
on:
  issue_comment:
    types: [created]
  pull_request_review_comment:
    types: [created]
  issues:
    types: [opened, assigned]

# claude-code-review.yml - Auto review
on:
  pull_request:
    types: [opened, synchronize]
```

## Documentation

Detailed documentation is available in the `docs/` folder:

| File | Description |
|------|-------------|
| `ARCHITECTURE.md` | Complete architecture design with diagrams |
| `API_SPECIFICATION.md` | Full REST API specification |
| `DATABASE_SCHEMA.md` | Database schema with DDL |
| `DESIGN_PLAN.md` | High-level design overview |
| `IMPLEMENTATION_TODO.md` | Implementation checklist with priorities |

## Common Tasks

### Adding a New Module

1. Create package: `kr.mclub.apiserver.{module}/`
2. Add `package-info.java` with `@ApplicationModule`
3. Create standard subpackages: `domain/`, `repository/`, `service/`, `api/`
4. Define allowed dependencies in `package-info.java`
5. Add Flyway migration for tables

### Adding a New Entity

1. Create entity in `{module}/domain/`
2. Extend `BaseEntity` or `BaseTimeEntity`
3. Create repository interface
4. Add Flyway migration: `V{next}__add_{entity}_table.sql`
5. Run `./gradlew flywayMigrate`

### Adding a New API Endpoint

1. Create DTO in `{module}/api/dto/`
2. Add service method in `{module}/service/`
3. Add controller method in `{module}/api/`
4. Document in `docs/API_SPECIFICATION.md`

### Publishing Domain Events

1. Define event record in `{module}/event/`
2. Create publisher service
3. Use `ApplicationEventPublisher.publishEvent()`
4. Create listener in subscribing module with `@ApplicationModuleListener`

## Security Notes

### Public Endpoints

```
/api/v1/auth/**        - Authentication
/api/v1/landing/**     - Landing page
/api/v1/events (GET)   - Event list
```

### Authentication

- OAuth2 providers: Google, Apple, Naver
- Passkey (WebAuthn) for passwordless login
- JWT tokens: Access (15min) + Refresh (7 days)

### Authorization

- Check user grade permission level
- Check board-specific permissions via PermissionGroup
- Admin endpoints require DIRECTOR (level 6) or higher

## Development Tips

1. **Module Isolation**: Never import classes from another module's `internal/` package
2. **Event Communication**: Use events for cross-module communication
3. **Lazy Loading**: Always use `FetchType.LAZY` for entity relationships
4. **Soft Delete**: Use `isDeleted` flag instead of hard delete for posts/comments
5. **Korean Domain Terms**: Keep domain-specific terms in Korean comments for clarity

## Troubleshooting

### Database Connection Issues

```bash
# Check if PostgreSQL is running
docker compose ps

# View PostgreSQL logs
docker compose logs postgres
```

### Build Failures

```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies

# Check for dependency conflicts
./gradlew dependencies --configuration compileClasspath
```

### Proto Generation Issues

```bash
# Regenerate gRPC stubs
./gradlew clean generateProto
```

---

*Last Updated: 2025-12-30*
