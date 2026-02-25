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
import com.sitepark.ies.label.core.usecase.UnassignLabelsFromEntitiesRequest;
import com.sitepark.ies.label.core.usecase.UnassignLabelsFromEntitiesResult;
import com.sitepark.ies.label.core.usecase.UnassignLabelsFromEntitiesUseCase;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.sharedkernel.security.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnassignLabelsFromEntitiesServiceTest {

  private UnassignLabelsFromEntitiesUseCase unassignLabelsFromEntitiesUseCase;
  private MultiEntityAuthorizationService authorizationService;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private UnassignLabelsFromEntitiesService service;

  @BeforeEach
  void setUp() {
    this.unassignLabelsFromEntitiesUseCase = mock();
    this.authorizationService = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new UnassignLabelsFromEntitiesService(
            unassignLabelsFromEntitiesUseCase,
            authorizationService,
            multiEntityNameResolver,
            auditLogServiceFactory);
    when(authorizationService.isWritable(any(EntityRef.class))).thenReturn(true);
    when(auditLogServiceFactory.createPerTypeForBatch(any(), any(), any(), any()))
        .thenReturn(Map.of("user", auditLogService));
    when(multiEntityNameResolver.resolveNames(any())).thenReturn(Map.of());
  }

  @Test
  void testUnassignLabelsFromEntitiesCallsUseCase() {

    EntityRef entityRef = EntityRef.of("user", "301");
    EntityLabelAssignment unassignment =
        EntityLabelAssignment.builder().assignments(entityRef, List.of("401")).build();
    Instant timestamp = Instant.now();
    UnassignLabelsFromEntitiesResult.Unassigned unassignedResult =
        new UnassignLabelsFromEntitiesResult.Unassigned(unassignment, timestamp);

    when(unassignLabelsFromEntitiesUseCase.unassignEntitiesFromLabels(
            any(UnassignLabelsFromEntitiesRequest.class)))
        .thenReturn(unassignedResult);

    UnassignLabelsFromEntitiesServiceRequest request =
        UnassignLabelsFromEntitiesServiceRequest.builder()
            .unassignEntitiesFromLabelsRequest(
                UnassignLabelsFromEntitiesRequest.builder()
                    .entityRefs(b -> b.add(entityRef))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    service.unassignLabelsFromEntities(request);

    verify(unassignLabelsFromEntitiesUseCase)
        .unassignEntitiesFromLabels(any(UnassignLabelsFromEntitiesRequest.class));
  }

  @Test
  void testUnassignLabelsFromEntitiesReturnsUnassignedCount() {

    EntityRef entityRef = EntityRef.of("user", "301");
    EntityLabelAssignment unassignment =
        EntityLabelAssignment.builder().assignments(entityRef, List.of("401")).build();
    Instant timestamp = Instant.now();
    UnassignLabelsFromEntitiesResult.Unassigned unassignedResult =
        new UnassignLabelsFromEntitiesResult.Unassigned(unassignment, timestamp);

    when(unassignLabelsFromEntitiesUseCase.unassignEntitiesFromLabels(
            any(UnassignLabelsFromEntitiesRequest.class)))
        .thenReturn(unassignedResult);

    UnassignLabelsFromEntitiesServiceRequest request =
        UnassignLabelsFromEntitiesServiceRequest.builder()
            .unassignEntitiesFromLabelsRequest(
                UnassignLabelsFromEntitiesRequest.builder()
                    .entityRefs(b -> b.add(entityRef))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    int count = service.unassignLabelsFromEntities(request);

    assertEquals(1, count, "Should return the count of unassigned entity-label pairs");
  }

  @Test
  void testUnassignLabelsFromEntitiesCreatesAuditLogWhenUnassigned() throws Exception {

    EntityRef entityRef = EntityRef.of("user", "301");
    EntityLabelAssignment unassignment =
        EntityLabelAssignment.builder().assignments(entityRef, List.of("401")).build();
    Instant timestamp = Instant.now();
    UnassignLabelsFromEntitiesResult.Unassigned unassignedResult =
        new UnassignLabelsFromEntitiesResult.Unassigned(unassignment, timestamp);

    when(unassignLabelsFromEntitiesUseCase.unassignEntitiesFromLabels(
            any(UnassignLabelsFromEntitiesRequest.class)))
        .thenReturn(unassignedResult);

    UnassignLabelsFromEntitiesServiceRequest request =
        UnassignLabelsFromEntitiesServiceRequest.builder()
            .unassignEntitiesFromLabelsRequest(
                UnassignLabelsFromEntitiesRequest.builder()
                    .entityRefs(b -> b.add(entityRef))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    service.unassignLabelsFromEntities(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUnassignLabelsFromEntitiesDoesNotCreateAuditLogWhenSkipped() {

    UnassignLabelsFromEntitiesResult.Skipped skippedResult =
        new UnassignLabelsFromEntitiesResult.Skipped();

    when(unassignLabelsFromEntitiesUseCase.unassignEntitiesFromLabels(
            any(UnassignLabelsFromEntitiesRequest.class)))
        .thenReturn(skippedResult);

    UnassignLabelsFromEntitiesServiceRequest request =
        UnassignLabelsFromEntitiesServiceRequest.builder()
            .unassignEntitiesFromLabelsRequest(
                UnassignLabelsFromEntitiesRequest.builder()
                    .entityRefs(b -> b.add(EntityRef.of("user", "301")))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    service.unassignLabelsFromEntities(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUnassignLabelsFromEntitiesThrowsAccessDeniedWhenEntityNotWritable() {

    when(authorizationService.isWritable(any(EntityRef.class))).thenReturn(false);

    UnassignLabelsFromEntitiesServiceRequest request =
        UnassignLabelsFromEntitiesServiceRequest.builder()
            .unassignEntitiesFromLabelsRequest(
                UnassignLabelsFromEntitiesRequest.builder()
                    .entityRefs(b -> b.add(EntityRef.of("user", "301")))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    assertThrows(
        AccessDeniedException.class,
        () -> service.unassignLabelsFromEntities(request),
        "Should throw AccessDeniedException when entity is not writable");
  }
}
