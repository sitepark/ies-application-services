package com.sitepark.ies.application.audit.revert;

import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.user.RevertAssignRolesToUsersActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertBatchAssignRolesToUsersActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertBatchReassignRolesToUsersActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertBatchRemoveUsersActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertBatchUnassignRolesToUsersActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertCreateUserActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertRemoveUserActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertUnassignRolesToUsersActionHandler;
import com.sitepark.ies.application.audit.revert.user.RevertUpdateUserActionHandler;
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
      RevertCreateUserActionHandler revertCreateUserActionHandler,
      RevertUpdateUserActionHandler revertUpdateUserActionHandler,
      RevertRemoveUserActionHandler revertRemoveUserActionHandler,
      RevertBatchRemoveUsersActionHandler revertBatchRemoveUsersActionHandler,
      RevertAssignRolesToUsersActionHandler revertAssignRolesToUsersActionHandler,
      RevertUnassignRolesToUsersActionHandler revertUnassignRolesToUsersActionHandler,
      RevertBatchAssignRolesToUsersActionHandler revertBatchAssignRolesToUsersActionHandler,
      RevertBatchReassignRolesToUsersActionHandler revertBatchReassignRolesToUsersActionHandler,
      RevertBatchUnassignRolesToUsersActionHandler revertBatchUnassignRolesToUsersActionHandler,
      RevertLabelActionHandler revertLabelActionHandler) {
    this.actionHandlers = new HashMap<>();
    this.actionHandlers.put(AuditLogAction.CREATE.name(), revertCreateUserActionHandler);
    this.actionHandlers.put(AuditLogAction.UPDATE.name(), revertUpdateUserActionHandler);
    this.actionHandlers.put(AuditLogAction.REMOVE.name(), revertRemoveUserActionHandler);
    this.actionHandlers.put(
        AuditLogAction.ASSIGN_ROLES_TO_USERS.name(), revertAssignRolesToUsersActionHandler);
    this.actionHandlers.put(
        AuditLogAction.UNASSIGN_ROLES_FROM_USERS.name(), revertUnassignRolesToUsersActionHandler);

    this.actionHandlers.put(
        AuditBatchLogAction.BATCH_REMOVE.name(), revertBatchRemoveUsersActionHandler);
    this.actionHandlers.put(
        AuditBatchLogAction.BATCH_ASSIGN_ROLES_TO_USERS.name(),
        revertBatchAssignRolesToUsersActionHandler);
    this.actionHandlers.put(
        AuditBatchLogAction.BATCH_REASSIGN_ROLES_TO_USERS.name(),
        revertBatchReassignRolesToUsersActionHandler);
    this.actionHandlers.put(
        AuditBatchLogAction.BATCH_UNASSIGN_ROLES_FROM_USERS.name(),
        revertBatchUnassignRolesToUsersActionHandler);

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
