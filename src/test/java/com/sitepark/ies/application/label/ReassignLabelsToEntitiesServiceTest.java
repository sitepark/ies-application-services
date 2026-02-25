package com.sitepark.ies.application.label;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityAuthorizationService;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.value.ReassignResult;
import com.sitepark.ies.label.core.domain.value.EntityLabelAssignment;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesRequest;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesResult;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesUseCase;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.sharedkernel.security.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReassignLabelsToEntitiesServiceTest {

  private ReassignLabelsToEntitiesUseCase reassignLabelsToEntitiesUseCase;
  private MultiEntityAuthorizationService authorizationService;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private ReassignLabelsToEntitiesService service;

  @BeforeEach
  void setUp() {
    this.reassignLabelsToEntitiesUseCase = mock();
    this.authorizationService = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new ReassignLabelsToEntitiesService(
            reassignLabelsToEntitiesUseCase,
            authorizationService,
            multiEntityNameResolver,
            auditLogServiceFactory);
    when(authorizationService.isWritable(any(EntityRef.class))).thenReturn(true);
    when(auditLogServiceFactory.createPerTypeForBatch(any(), any(), any(), any()))
        .thenReturn(Map.of("user", auditLogService));
    when(multiEntityNameResolver.resolveNames(any())).thenReturn(Map.of());
  }

  @Test
  void testReassignEntitiesFromLabelsCallsUseCase() {

    EntityRef entityRef = EntityRef.of("user", "301");
    EntityLabelAssignment assignments =
        EntityLabelAssignment.builder().assignments(entityRef, List.of("401")).build();
    EntityLabelAssignment unassignments = EntityLabelAssignment.builder().build();
    Instant timestamp = Instant.now();
    ReassignLabelsToEntitiesResult.Reassigned reassignedResult =
        new ReassignLabelsToEntitiesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignLabelsToEntitiesUseCase.reassignLabelsToEntities(
            any(ReassignLabelsToEntitiesRequest.class)))
        .thenReturn(reassignedResult);

    ReassignLabelsToEntitiesServiceRequest request =
        ReassignLabelsToEntitiesServiceRequest.builder()
            .reassignLabelsToEntitiesRequest(
                ReassignLabelsToEntitiesRequest.builder()
                    .entityRefs(b -> b.add(entityRef))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    service.reassignEntitiesFromLabels(request);

    verify(reassignLabelsToEntitiesUseCase)
        .reassignLabelsToEntities(any(ReassignLabelsToEntitiesRequest.class));
  }

  @Test
  void testReassignEntitiesFromLabelsReturnsReassignResult() {

    EntityRef entityRef = EntityRef.of("user", "301");
    EntityLabelAssignment assignments =
        EntityLabelAssignment.builder().assignments(entityRef, List.of("401")).build();
    EntityLabelAssignment unassignments =
        EntityLabelAssignment.builder().assignments(entityRef, List.of("402")).build();
    Instant timestamp = Instant.now();
    ReassignLabelsToEntitiesResult.Reassigned reassignedResult =
        new ReassignLabelsToEntitiesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignLabelsToEntitiesUseCase.reassignLabelsToEntities(
            any(ReassignLabelsToEntitiesRequest.class)))
        .thenReturn(reassignedResult);

    ReassignLabelsToEntitiesServiceRequest request =
        ReassignLabelsToEntitiesServiceRequest.builder()
            .reassignLabelsToEntitiesRequest(
                ReassignLabelsToEntitiesRequest.builder()
                    .entityRefs(b -> b.add(entityRef))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    ReassignResult result = service.reassignEntitiesFromLabels(request);

    assertEquals(
        new ReassignResult(1, 1),
        result,
        "Should return reassign result with assigned/unassigned counts");
  }

  @Test
  void testReassignEntitiesFromLabelsReturnsEmptyResultWhenSkipped() {

    ReassignLabelsToEntitiesResult.Skipped skippedResult =
        new ReassignLabelsToEntitiesResult.Skipped();

    when(reassignLabelsToEntitiesUseCase.reassignLabelsToEntities(
            any(ReassignLabelsToEntitiesRequest.class)))
        .thenReturn(skippedResult);

    ReassignLabelsToEntitiesServiceRequest request =
        ReassignLabelsToEntitiesServiceRequest.builder()
            .reassignLabelsToEntitiesRequest(
                ReassignLabelsToEntitiesRequest.builder()
                    .entityRefs(b -> b.add(EntityRef.of("user", "301")))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    ReassignResult result = service.reassignEntitiesFromLabels(request);

    assertEquals(ReassignResult.empty(), result, "Should return empty result when skipped");
  }

  @Test
  void testReassignEntitiesFromLabelsCreatesAuditLogWhenReassigned() throws Exception {

    EntityRef entityRef = EntityRef.of("user", "301");
    EntityLabelAssignment assignments =
        EntityLabelAssignment.builder().assignments(entityRef, List.of("401")).build();
    EntityLabelAssignment unassignments = EntityLabelAssignment.builder().build();
    Instant timestamp = Instant.now();
    ReassignLabelsToEntitiesResult.Reassigned reassignedResult =
        new ReassignLabelsToEntitiesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignLabelsToEntitiesUseCase.reassignLabelsToEntities(
            any(ReassignLabelsToEntitiesRequest.class)))
        .thenReturn(reassignedResult);

    ReassignLabelsToEntitiesServiceRequest request =
        ReassignLabelsToEntitiesServiceRequest.builder()
            .reassignLabelsToEntitiesRequest(
                ReassignLabelsToEntitiesRequest.builder()
                    .entityRefs(b -> b.add(entityRef))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    service.reassignEntitiesFromLabels(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testReassignEntitiesFromLabelsDoesNotCreateAuditLogWhenSkipped() {

    ReassignLabelsToEntitiesResult.Skipped skippedResult =
        new ReassignLabelsToEntitiesResult.Skipped();

    when(reassignLabelsToEntitiesUseCase.reassignLabelsToEntities(
            any(ReassignLabelsToEntitiesRequest.class)))
        .thenReturn(skippedResult);

    ReassignLabelsToEntitiesServiceRequest request =
        ReassignLabelsToEntitiesServiceRequest.builder()
            .reassignLabelsToEntitiesRequest(
                ReassignLabelsToEntitiesRequest.builder()
                    .entityRefs(b -> b.add(EntityRef.of("user", "301")))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    service.reassignEntitiesFromLabels(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testReassignEntitiesFromLabelsCreatesTwoAuditLogsWhenBothAssignedAndUnassigned()
      throws Exception {

    EntityRef entityRef1 = EntityRef.of("user", "301");
    EntityRef entityRef2 = EntityRef.of("user", "302");
    EntityLabelAssignment assignments =
        EntityLabelAssignment.builder().assignments(entityRef1, List.of("401")).build();
    EntityLabelAssignment unassignments =
        EntityLabelAssignment.builder().assignments(entityRef2, List.of("402")).build();
    Instant timestamp = Instant.now();
    ReassignLabelsToEntitiesResult.Reassigned reassignedResult =
        new ReassignLabelsToEntitiesResult.Reassigned(assignments, unassignments, timestamp);

    when(reassignLabelsToEntitiesUseCase.reassignLabelsToEntities(
            any(ReassignLabelsToEntitiesRequest.class)))
        .thenReturn(reassignedResult);
    when(auditLogServiceFactory.createPerTypeForBatch(any(), any(), any(), any()))
        .thenReturn(Map.of("user", auditLogService));

    ReassignLabelsToEntitiesServiceRequest request =
        ReassignLabelsToEntitiesServiceRequest.builder()
            .reassignLabelsToEntitiesRequest(
                ReassignLabelsToEntitiesRequest.builder()
                    .entityRefs(b -> b.add(entityRef1))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    service.reassignEntitiesFromLabels(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testReassignEntitiesFromLabelsThrowsAccessDeniedWhenEntityNotWritable() {

    when(authorizationService.isWritable(any(EntityRef.class))).thenReturn(false);

    ReassignLabelsToEntitiesServiceRequest request =
        ReassignLabelsToEntitiesServiceRequest.builder()
            .reassignLabelsToEntitiesRequest(
                ReassignLabelsToEntitiesRequest.builder()
                    .entityRefs(b -> b.add(EntityRef.of("user", "301")))
                    .labelIdentifiers(b -> b.id("401"))
                    .build())
            .build();

    assertThrows(
        AccessDeniedException.class,
        () -> service.reassignEntitiesFromLabels(request),
        "Should throw AccessDeniedException when entity is not writable");
  }
}
