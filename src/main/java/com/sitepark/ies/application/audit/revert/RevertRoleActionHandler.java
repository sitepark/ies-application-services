package com.sitepark.ies.application.audit.revert;

import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.role.RevertRoleAssignPrivilegesActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertRoleBatchRemoveActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertRoleBatchUnassignPrivilegesActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertRoleCreateActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertRoleRemoveActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertRoleUnassignPrivilegesActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertRoleUpdateActionHandler;
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
      RevertRoleCreateActionHandler createHandler,
      RevertRoleUpdateActionHandler updateHandler,
      RevertRoleRemoveActionHandler removeHandler,
      RevertRoleBatchRemoveActionHandler batchRemoveHandler,
      RevertRoleAssignPrivilegesActionHandler assignPrivilegesHandler,
      RevertRoleAssignPrivilegesActionHandler batchAssignPrivilegesHandler,
      RevertRoleUnassignPrivilegesActionHandler unassignPrivilegesActionHandler,
      RevertRoleBatchUnassignPrivilegesActionHandler batchUnassignPrivilegesActionHandler) {

    this.actionHandlers = new HashMap<>();
    this.actionHandlers.put(AuditLogAction.CREATE.name(), createHandler);
    this.actionHandlers.put(AuditLogAction.UPDATE.name(), updateHandler);
    this.actionHandlers.put(AuditLogAction.REMOVE.name(), removeHandler);
    this.actionHandlers.put(AuditLogAction.ASSIGN_PRIVILEGES.name(), assignPrivilegesHandler);
    this.actionHandlers.put(
        AuditLogAction.UNASSIGN_PRIVILEGES.name(), unassignPrivilegesActionHandler);
    this.actionHandlers.put(AuditBatchLogAction.BATCH_REMOVE.name(), batchRemoveHandler);
    this.actionHandlers.put(
        AuditBatchLogAction.BATCH_ASSIGN_PRIVILEGES.name(), batchAssignPrivilegesHandler);
    this.actionHandlers.put(
        AuditBatchLogAction.BATCH_UNASSIGN_PRIVILEGES.name(), batchUnassignPrivilegesActionHandler);
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
