package com.sitepark.ies.application.audit.revert;

import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.privilege.RevertPrivilegeBatchRemoveActionHandler;
import com.sitepark.ies.application.audit.revert.privilege.RevertPrivilegeCreateActionHandler;
import com.sitepark.ies.application.audit.revert.privilege.RevertPrivilegeRemoveActionHandler;
import com.sitepark.ies.application.audit.revert.privilege.RevertPrivilegeUpdateActionHandler;
import com.sitepark.ies.audit.core.service.ReverseActionHandler;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class RevertPrivilegeActionHandler implements ReverseActionHandler {

  private final Map<String, RevertEntityActionHandler> actionHandlers;

  @Inject
  RevertPrivilegeActionHandler(
      RevertPrivilegeCreateActionHandler createHandler,
      RevertPrivilegeUpdateActionHandler updateHandler,
      RevertPrivilegeRemoveActionHandler removeHandler,
      RevertPrivilegeBatchRemoveActionHandler batchRemoveHandler) {
    this.actionHandlers = new HashMap<>();
    this.actionHandlers.put(AuditLogAction.CREATE.name(), createHandler);
    this.actionHandlers.put(AuditLogAction.UPDATE.name(), updateHandler);
    this.actionHandlers.put(AuditLogAction.REMOVE.name(), removeHandler);
    this.actionHandlers.put(AuditBatchLogAction.BATCH_REMOVE.name(), batchRemoveHandler);
  }

  @Override
  public String getEntityType() {
    return EntityRef.toTypeString(Privilege.class);
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
