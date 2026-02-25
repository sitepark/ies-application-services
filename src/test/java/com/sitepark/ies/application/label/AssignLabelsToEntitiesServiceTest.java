package com.sitepark.ies.application.label;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityAuthorizationService;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.label.core.domain.value.EntityLabelAssignment;
import com.sitepark.ies.label.core.usecase.AssignLabelsToEntitiesRequest;
import com.sitepark.ies.label.core.usecase.AssignLabelsToEntitiesResult;
import com.sitepark.ies.label.core.usecase.AssignLabelsToEntitiesUseCase;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.sharedkernel.security.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssignLabelsToEntitiesServiceTest {

  private AssignLabelsToEntitiesUseCase assignLabelsToEntitiesUseCase;
  private MultiEntityAuthorizationService authorizationService;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private AssignLabelsToEntitiesService service;

  @BeforeEach
  void setUp() {
    this.assignLabelsToEntitiesUseCase = mock();
    this.authorizationService = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new AssignLabelsToEntitiesService(
            assignLabelsToEntitiesUseCase,
            authorizationService,
            multiEntityNameResolver,
            auditLogServiceFactory);
    when(authorizationService.isWritable(any(EntityRef.class))).thenReturn(true);
    when(auditLogServiceFactory.createPerTypeForBatch(any(), any(), any(), any()))
        .thenReturn(Map.of("user", auditLogService));
    when(multiEntityNameResolver.resolveNames(any())).thenReturn(Map.of());
  }

  @Test
  void testAssignLabelsToEntitiesCallsUseCase() {

    EntityRef entityRef = EntityRef.of("user", "301");
    EntityLabelAssignment assignment =
        EntityLabelAssignment.builder().assignments(entityRef, List.of("401")).build();
    Instant timestamp = Instant.now();
    AssignLabelsToEntitiesResult.Assigned assignedResult =
        new AssignLabelsToEntitiesResult.Assigned(assignment, timestamp);

    when(assignLabelsToEntitiesUseCase.assignEntitiesToLabels(
            any(AssignLabelsToEntitiesRequest.class)))
        .thenReturn(assignedResult);

    AssignLabelsToEntitiesServiceRequest request =
        AssignLabelsToEntitiesServiceRequest.builder()
            .assignLabelsToEntitiesRequest(
                AssignLabelsToEntitiesRequest.builder()
                    .entityRefs(b -> b.add(entityRef))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    service.assignLabelsToEntities(request);

    verify(assignLabelsToEntitiesUseCase)
        .assignEntitiesToLabels(any(AssignLabelsToEntitiesRequest.class));
  }

  @Test
  void testAssignLabelsToEntitiesReturnsAssignedCount() {

    EntityRef entityRef = EntityRef.of("user", "301");
    EntityLabelAssignment assignment =
        EntityLabelAssignment.builder().assignments(entityRef, List.of("401")).build();
    Instant timestamp = Instant.now();
    AssignLabelsToEntitiesResult.Assigned assignedResult =
        new AssignLabelsToEntitiesResult.Assigned(assignment, timestamp);

    when(assignLabelsToEntitiesUseCase.assignEntitiesToLabels(
            any(AssignLabelsToEntitiesRequest.class)))
        .thenReturn(assignedResult);

    AssignLabelsToEntitiesServiceRequest request =
        AssignLabelsToEntitiesServiceRequest.builder()
            .assignLabelsToEntitiesRequest(
                AssignLabelsToEntitiesRequest.builder()
                    .entityRefs(b -> b.add(entityRef))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    int count = service.assignLabelsToEntities(request);

    assertEquals(1, count, "Should return the count of assigned entity refs");
  }

  @Test
  void testAssignLabelsToEntitiesCreatesAuditLogWhenAssigned() throws Exception {

    EntityRef entityRef = EntityRef.of("user", "301");
    EntityLabelAssignment assignment =
        EntityLabelAssignment.builder().assignments(entityRef, List.of("401")).build();
    Instant timestamp = Instant.now();
    AssignLabelsToEntitiesResult.Assigned assignedResult =
        new AssignLabelsToEntitiesResult.Assigned(assignment, timestamp);

    when(assignLabelsToEntitiesUseCase.assignEntitiesToLabels(
            any(AssignLabelsToEntitiesRequest.class)))
        .thenReturn(assignedResult);

    AssignLabelsToEntitiesServiceRequest request =
        AssignLabelsToEntitiesServiceRequest.builder()
            .assignLabelsToEntitiesRequest(
                AssignLabelsToEntitiesRequest.builder()
                    .entityRefs(b -> b.add(entityRef))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    service.assignLabelsToEntities(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testAssignLabelsToEntitiesDoesNotCreateAuditLogWhenSkipped() {

    AssignLabelsToEntitiesResult.Skipped skippedResult = new AssignLabelsToEntitiesResult.Skipped();

    when(assignLabelsToEntitiesUseCase.assignEntitiesToLabels(
            any(AssignLabelsToEntitiesRequest.class)))
        .thenReturn(skippedResult);

    AssignLabelsToEntitiesServiceRequest request =
        AssignLabelsToEntitiesServiceRequest.builder()
            .assignLabelsToEntitiesRequest(
                AssignLabelsToEntitiesRequest.builder()
                    .entityRefs(b -> b.add(EntityRef.of("user", "301")))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    service.assignLabelsToEntities(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testAssignLabelsToEntitiesThrowsAccessDeniedWhenEntityNotWritable() {

    when(authorizationService.isWritable(any(EntityRef.class))).thenReturn(false);

    AssignLabelsToEntitiesServiceRequest request =
        AssignLabelsToEntitiesServiceRequest.builder()
            .assignLabelsToEntitiesRequest(
                AssignLabelsToEntitiesRequest.builder()
                    .entityRefs(b -> b.add(EntityRef.of("user", "301")))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    assertThrows(
        AccessDeniedException.class,
        () -> service.assignLabelsToEntities(request),
        "Should throw AccessDeniedException when entity is not writable");
  }
}
