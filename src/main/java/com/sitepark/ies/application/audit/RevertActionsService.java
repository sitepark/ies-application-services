package com.sitepark.ies.application.audit;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.audit.core.usecase.RevertActionsRequest;
import com.sitepark.ies.audit.core.usecase.RevertActionsUseCase;
import jakarta.inject.Inject;
import java.time.Clock;
import java.time.Instant;

public final class RevertActionsService {

  private final RevertActionsUseCase revertActionsUseCase;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final Clock clock;

  @Inject
  RevertActionsService(
      RevertActionsUseCase revertActionsUseCase,
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      Clock clock) {
    this.revertActionsUseCase = revertActionsUseCase;
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.clock = clock;
  }

  public void revert(RevertActionsServiceRequest request) {

    Instant timestamp = Instant.now(this.clock);

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(timestamp, request.auditParentId());

    String parentId =
        request.auditLogIds().size() > 1
            ? auditLogService.createBatchLog(null, AuditBatchLogAction.REVERT_BATCH)
            : request.auditParentId();
    auditLogService.updateParentId(parentId);

    revertActionsUseCase.revert(new RevertActionsRequest(request.auditLogIds(), parentId));
  }
}
