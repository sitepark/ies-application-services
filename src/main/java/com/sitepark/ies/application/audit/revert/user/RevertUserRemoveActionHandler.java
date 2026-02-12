package com.sitepark.ies.application.audit.revert.user;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
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

public class RevertUserRemoveActionHandler implements RevertEntityActionHandler {

  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final AuditLogService auditLogService;
  private final RestoreUserUseCase restoreUserUseCase;

  @Inject
  RevertUserRemoveActionHandler(
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      AuditLogService auditLogService,
      RestoreUserUseCase restoreUserUseCase) {
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.auditLogService = auditLogService;
    this.restoreUserUseCase = restoreUserUseCase;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      UserSnapshot restoreData =
          this.auditLogService.deserialize(request.backwardData(), UserSnapshot.class);

      RestoreUserResult result =
          this.restoreUserUseCase.restoreUser(new RestoreUserRequest(restoreData));

      if (result instanceof RestoreUserResult.Restored restored) {
        this.createRestoreAuditLog(restored, request.parentId());
      }

    } catch (IOException e) {
      throw new RevertFailedException(request, "Failed to deserialize user-snapshot", e);
    }
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
