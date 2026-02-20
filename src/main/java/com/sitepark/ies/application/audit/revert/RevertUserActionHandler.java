package com.sitepark.ies.application.audit.revert;

import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.user.RevertUserAssignRolesActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertUserBatchAssignRolesActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertUserBatchRemoveActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertUserBatchUnassignRolesActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertUserCreateActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertUserRemoveActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertUserUnassignRolesActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertUserUpdateActionHandler;
import com.sitepark.ies.audit.core.service.ReverseActionHandler;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class RevertUserActionHandler implements ReverseActionHandler {

  private final Map<String, RevertEntityActionHandler> actionHandlers;

  @Inject
  RevertUserActionHandler(
      RevertUserCreateActionHandler createHandler,
      RevertUserUpdateActionHandler updateHandler,
      RevertUserRemoveActionHandler removeHandler,
      RevertUserBatchRemoveActionHandler batchRemoveHandler,
      RevertUserAssignRolesActionHandler assignRolesHandler,
      RevertUserBatchAssignRolesActionHandler batchAssignRolesHandler,
      RevertUserUnassignRolesActionHandler unassignRolesHandler,
      RevertUserBatchUnassignRolesActionHandler batchUnassignRolesHandler,
      RevertLabelActionHandler revertLabelActionHandler) {
    this.actionHandlers = new HashMap<>();
    this.actionHandlers.put(AuditLogAction.CREATE.name(), createHandler);
    this.actionHandlers.put(AuditLogAction.UPDATE.name(), updateHandler);
    this.actionHandlers.put(AuditLogAction.REMOVE.name(), removeHandler);
    this.actionHandlers.put(AuditLogAction.ASSIGN_ROLES.name(), assignRolesHandler);
    this.actionHandlers.put(AuditLogAction.UNASSIGN_ROLES.name(), unassignRolesHandler);
    this.actionHandlers.put(AuditBatchLogAction.BATCH_REMOVE.name(), batchRemoveHandler);
    this.actionHandlers.put(AuditBatchLogAction.BATCH_ASSIGN_ROLES.name(), batchAssignRolesHandler);
    this.actionHandlers.put(
        AuditBatchLogAction.BATCH_UNASSIGN_ROLES.name(), batchUnassignRolesHandler);

    this.actionHandlers.putAll(revertLabelActionHandler.getEntitiesActionHandlers());
  }

  @Override
  public String getEntityType() {
    return EntityRef.toTypeString(User.class);
  }

  @Override
  public void revert(RevertRequest request) {
    RevertEntityActionHandler handler = this.actionHandlers.get(request.action());
    if (handler == null) {
      throw new IllegalArgumentException("Unsupported action: " + request.action());
    }
    handler.revert(request);
  }
}
