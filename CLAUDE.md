# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ies-application-services** is the Application Layer of the IES (Identity and Entity System) architecture. This module orchestrates operations across multiple bounded contexts following Clean Architecture and Domain-Driven Design principles.

**Key Principle**: Application Services coordinate use cases from different bounded contexts but contain NO business logic. They are thin orchestration layers.

## Build and Development Commands

### Build and Compile
```bash
# Clean and compile
mvn clean compile

# Full build with all checks
mvn clean verify
```

### Testing
```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=UserApplicationServiceTest

# Run specific test method
mvn test -Dtest=UserApplicationServiceTest#testCreateUserWithPassword
```

### Code Quality
```bash
# Apply code formatting
mvn spotless:apply

# Check code formatting
mvn spotless:check

# Run SpotBugs analysis
mvn spotbugs:check

# Run PMD analysis
mvn pmd:check

# Run all quality checks (included in verify)
mvn verify
```

### Coverage
```bash
# Generate JaCoCo coverage report
mvn jacoco:report

# Report is generated at: target/site/jacoco/index.html
```

## Architecture and Structure

### Architectural Position

This module sits between the **Presentation Layer** (controllers) and the **Domain Layer** (bounded contexts):

```
Presentation Layer (GraphQL, REST, CLI)
         ↓
   Application Layer (THIS MODULE) ← Orchestrates workflows
         ↓
Domain Layer (userrepository-core, security-core, audit-core)
```

### Module Dependencies

**This module depends on:**
- `ies-userrepository-core` - User management domain and use cases
- `ies-security-core` - Security, authentication, password management
- `ies-audit-core` - Audit logging (optional)
- `ies-shared-kernel` - Shared domain primitives and base types

**This module is used by:**
- `ies-userrepository-graphql-extension` - GraphQL API layer
- `ies-userrepository-rest-extension` - REST API layer (if exists)
- Any other presentation layer requiring cross-context workflows

### Package Structure

```
com.sitepark.ies.application/
├── user/                          # User-related workflows
│   ├── UserApplicationService     # Orchestrates user creation with password
│   └── CreateUserWithPasswordRequest
├── role/                          # Future: Role-related workflows
└── security/                      # Future: Authentication workflows
```

## When to Use Application Services

### ✅ Use Application Services When:
- Orchestrating **multiple use cases** from **different bounded contexts**
- The same workflow is needed in **multiple controllers** (avoiding duplication)
- Managing **transactional boundaries** across contexts
- Coordinating between userrepository-core AND security-core (or other combinations)

### ⛔ Do NOT Use Application Services When:
- Calling **only one use case** → Controllers should call use cases directly
- Operation is **bounded-context-specific** → Keep it in the domain layer
- Adding **business logic** → That belongs in use cases or domain entities

## Development Guidelines

### Application Service Design

Application services must be **thin orchestration layers**:
- ✅ Call use cases from different bounded contexts
- ✅ Pass data between contexts
- ✅ Handle transaction boundaries
- ✅ Provide convenient APIs for controllers
- ❌ NO business logic
- ❌ NO validation (that's in use cases)
- ❌ NO domain rules (that's in entities)

### Example Pattern

```java
public class UserApplicationService {

  private final CreateUserUseCase createUserUseCase;
  private final SetUserPasswordUseCase setUserPasswordUseCase;

  @Inject
  UserApplicationService(
      CreateUserUseCase createUserUseCase,
      SetUserPasswordUseCase setUserPasswordUseCase) {
    this.createUserUseCase = createUserUseCase;
    this.setUserPasswordUseCase = setUserPasswordUseCase;
  }

  public String createUserWithPassword(@NotNull CreateUserWithPasswordRequest request) {
    // 1. Create user (userrepository-core)
    String userId = this.createUserUseCase.createUser(...);

    // 2. Set password (security-core) - if provided
    if (request.password() != null && !request.password().isBlank()) {
      this.setUserPasswordUseCase.setUserPassword(...);
    }

    return userId;
  }
}
```

### Testing Application Services

Application services are tested by **mocking the use cases** they orchestrate:

```java
@Test
void testCreateUserWithPassword() {
  // Mock use cases
  when(createUserUseCase.createUser(any())).thenReturn("123");

  // Execute
  String userId = service.createUserWithPassword(request);

  // Verify orchestration
  verify(createUserUseCase).createUser(any());
  verify(setUserPasswordUseCase).setUserPassword(any());
  assertEquals("123", userId, "Should return the created user ID");
}
```

## Java Module System

This project uses Java Platform Module System (JPMS). The module descriptor is in `src/main/java/module-info.java`:

```java
module com.sitepark.ies.application {
  exports com.sitepark.ies.application.user;

  requires com.sitepark.ies.userrepository.core;
  requires com.sitepark.ies.security.core;
  requires com.sitepark.ies.sharedkernel;
  requires jakarta.inject;
  requires org.apache.logging.log4j;
  requires static org.jetbrains.annotations;
}
```

When adding new packages, remember to export them in `module-info.java`.

## Technology Stack

- **Java**: 21 (LTS)
- **Build Tool**: Maven 3.8+
- **DI Framework**: Jakarta Inject (JSR-330)
- **Testing**: JUnit 5, Mockito
- **Logging**: Log4j2
- **Code Quality**: Spotless, SpotBugs, PMD, JaCoCo

## Code Quality Requirements

All code must pass:
1. **Spotless** formatting (Google Java Style)
2. **SpotBugs** static analysis
3. **PMD** rule checks (see `pmd-ruleset.xml`)
4. **JaCoCo** coverage checks (currently set to 0% minimum)
5. **Compiler** warnings (zero warnings policy)

## Project-Specific Patterns

### Request Objects
Application service methods use immutable request objects built with the Builder pattern:
- `CreateUserWithPasswordRequest` - Contains user, password, roleIdentifiers, auditParentId
- Request objects are located in the same package as their service

### Logging
Application services log at appropriate levels:
- `DEBUG`: Detailed workflow steps
- `INFO`: Successful completion of operations
- Use structured logging with clear context

### Dependency Injection
Use Jakarta Inject annotations:
- `@Inject` on constructors (package-private for application services)
- Use `Provider<ApplicationService>` in controllers for request-scoped services
