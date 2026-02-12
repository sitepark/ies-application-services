package com.sitepark.ies.application.audit.revert.user;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.domain.value.UserSnapshot;
import com.sitepark.ies.userrepository.core.usecase.user.RestoreUserRequest;
import com.sitepark.ies.userrepository.core.usecase.user.RestoreUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.RestoreUserUseCase;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class RevertUserBatchRemoveActionHandler implements RevertEntityActionHandler {

  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final AuditLogService auditLogService;
  private final RestoreUserUseCase restoreUserUseCase;
  private final Clock clock;

  @Inject
  RevertUserBatchRemoveActionHandler(
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      AuditLogService auditLogService,
      RestoreUserUseCase restoreUserUseCase,
      Clock clock) {
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.auditLogService = auditLogService;
    this.restoreUserUseCase = restoreUserUseCase;
    this.clock = clock;
  }

  @Override
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public void revert(RevertRequest request) {
    List<String> childIds = this.auditLogService.getRecursiveChildIds(request.id());
    if (childIds.isEmpty()) {
      return;
    }

    Instant timestamp = Instant.now(this.clock);
    String auditLogParentId = this.createRevertBatchRemoveLog(timestamp, request.parentId());

    for (String childId : childIds) {
      UserSnapshot restoreData;
      try {
        Optional<UserSnapshot> dataOpt =
            this.auditLogService.getBackwardData(childId, UserSnapshot.class);
        restoreData =
            dataOpt.orElseThrow(
                () -> new RevertFailedException(request, "No backward data for log " + childId));
      } catch (IOException e) {
        throw new RevertFailedException(request, "Failed to deserialize user-snapshot", e);
      }

      RestoreUserResult result =
          this.restoreUserUseCase.restoreUser(new RestoreUserRequest(restoreData));

      if (result instanceof RestoreUserResult.Restored restored) {
        this.createRestoreAuditLog(restored, auditLogParentId);
      }
    }
  }

  private String createRevertBatchRemoveLog(Instant timestamp, String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(timestamp, auditParentId);
    return auditLogService.createBatchLog(User.class, AuditBatchLogAction.REVERT_BATCH_REMOVE);
  }

  private void createRestoreAuditLog(RestoreUserResult.Restored restored, String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(restored.timestamp(), auditParentId);

    auditLogService.createLog(
        AuditLogTarget.of(
            User.class, restored.userId(), restored.snapshot().user().toDisplayName()),
        AuditLogAction.RESTORE,
        null,
        restored.snapshot());
  }
}
