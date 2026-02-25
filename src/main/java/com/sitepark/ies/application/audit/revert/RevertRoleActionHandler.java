package com.sitepark.ies.application.audit.revert;

import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.role.RevertAssignPrivilegesToRolesActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertBatchAssignPrivilegesToRolesActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertBatchRemoveRolesActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertBatchUnassignPrivilegesToRolesActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertCreateRoleActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertRemoveRoleActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertRoleUpdateActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertUnassignPrivilegesFromRolesActionHandler;
import com.sitepark.ies.audit.core.service.ReverseActionHandler;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class RevertRoleActionHandler implements ReverseActionHandler {

  private final Map<String, RevertEntityActionHandler> actionHandlers;

  @Inject
  RevertRoleActionHandler(
      RevertCreateRoleActionHandler revertCreateRoleActionHandler,
      RevertRoleUpdateActionHandler revertRoleUpdateActionHandler,
      RevertRemoveRoleActionHandler revertRemoveRoleActionHandler,
      RevertBatchRemoveRolesActionHandler revertBatchRemoveRolesActionHandler,
      RevertAssignPrivilegesToRolesActionHandler revertAssignPrivilegesToRolesActionHandler,
      RevertUnassignPrivilegesFromRolesActionHandler revertUnassignPrivilegesFromRolesActionHandler,
      RevertBatchAssignPrivilegesToRolesActionHandler
          revertBatchAssignPrivilegesToRolesActionHandler,
      RevertBatchUnassignPrivilegesToRolesActionHandler
          revertBatchUnassignPrivilegesToRolesActionHandler,
      RevertLabelActionHandler revertLabelActionHandler) {

    this.actionHandlers = new HashMap<>();
    this.actionHandlers.put(AuditLogAction.CREATE.name(), revertCreateRoleActionHandler);
    this.actionHandlers.put(AuditLogAction.UPDATE.name(), revertRoleUpdateActionHandler);
    this.actionHandlers.put(AuditLogAction.REMOVE.name(), revertRemoveRoleActionHandler);
    this.actionHandlers.put(
        AuditLogAction.ASSIGN_PRIVILEGES_TO_ROLES.name(),
        revertAssignPrivilegesToRolesActionHandler);
    this.actionHandlers.put(
        AuditLogAction.UNASSIGN_PRIVILEGES_FROM_ROLES.name(),
        revertUnassignPrivilegesFromRolesActionHandler);
    this.actionHandlers.put(
        AuditBatchLogAction.BATCH_REMOVE.name(), revertBatchRemoveRolesActionHandler);

    this.actionHandlers.put(
        AuditBatchLogAction.BATCH_ASSIGN_PRIVILEGES_TO_ROLES.name(),
        revertBatchAssignPrivilegesToRolesActionHandler);
    this.actionHandlers.put(
        AuditBatchLogAction.BATCH_UNASSIGN_PRIVILEGES_FROM_ROLES.name(),
        revertBatchUnassignPrivilegesToRolesActionHandler);

    this.actionHandlers.putAll(revertLabelActionHandler.getEntitiesActionHandlers());
  }

  @Override
  public String getEntityType() {
    return EntityRef.toTypeString(Role.class);
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
