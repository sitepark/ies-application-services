package com.sitepark.ies.application.audit.revert.label;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.label.AssignLabelsToEntitiesService;
import com.sitepark.ies.application.label.AssignLabelsToEntitiesServiceRequest;
import com.sitepark.ies.audit.core.domain.entity.AuditLog;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.usecase.AssignLabelsToEntitiesRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class RevertLabelBatchUnassignEntitiesActionHandler implements RevertEntityActionHandler {

  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final AuditLogService auditLogService;
  private final AssignLabelsToEntitiesService assignLabelsToEntitiesService;
  private final Clock clock;

  @Inject
  RevertLabelBatchUnassignEntitiesActionHandler(
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      AuditLogService auditLogService,
      AssignLabelsToEntitiesService assignLabelsToEntitiesService,
      Clock clock) {
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.auditLogService = auditLogService;
    this.assignLabelsToEntitiesService = assignLabelsToEntitiesService;
    this.clock = clock;
  }

  @Override
  public void revert(RevertRequest request) {
    List<String> childIds = this.auditLogService.getRecursiveChildIds(request.id());
    if (childIds.isEmpty()) {
      return;
    }

    Instant timestamp = Instant.now(this.clock);
    ApplicationAuditLogService auditLogService =
        this.createRevertBatchUnassignLabelsFromEntitiesLog(timestamp, request.parentId());

    for (String childId : childIds) {
      EntityRef entityRef;
      List<String> labelIds;
      try {
        Optional<AuditLog> auditLogOpt = this.auditLogService.getAuditLog(childId);
        if (auditLogOpt.isEmpty()) {
          continue;
        }
        AuditLog auditLog = auditLogOpt.get();
        entityRef = EntityRef.of(auditLog.entityType(), auditLog.entityId());
        labelIds = this.auditLogService.deserializeList(auditLog.backwardData(), String.class);
      } catch (IOException e) {
        throw new RevertFailedException(request, "Failed to deserialize labelIds", e);
      }
      this.assignLabelsToEntitiesService.assignLabelsToEntities(
          AssignLabelsToEntitiesServiceRequest.builder()
              .assignEntitiesToLabelsRequest(
                  AssignLabelsToEntitiesRequest.builder()
                      .entityRefs(b -> b.add(entityRef))
                      .labelIdentifiers(b -> b.ids(labelIds))
                      .build())
              .auditParentId(auditLogService.parentId())
              .build());
    }
  }

  private ApplicationAuditLogService createRevertBatchUnassignLabelsFromEntitiesLog(
      Instant timestamp, String auditParentId) {
    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(timestamp, auditParentId);
    String batchId =
        auditLogService.createBatchLog(
            Label.class, AuditBatchLogAction.REVERT_BATCH_UNASSIGN_LABELS_FROM_ENTITIES);
    auditLogService.updateParentId(batchId);
    return auditLogService;
  }
}
