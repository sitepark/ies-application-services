package com.sitepark.ies.application.audit.revert;

import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.label.RevertLabelAssignEntitiesActionHandler;
import com.sitepark.ies.application.audit.revert.label.RevertLabelBatchAssignEntitiesActionHandler;
import com.sitepark.ies.application.audit.revert.label.RevertLabelBatchUnassignEntitiesActionHandler;
import com.sitepark.ies.application.audit.revert.label.RevertLabelCreateActionHandler;
import com.sitepark.ies.application.audit.revert.label.RevertLabelRemoveActionHandler;
import com.sitepark.ies.application.audit.revert.label.RevertLabelUnassignEntitiesActionHandler;
import com.sitepark.ies.application.audit.revert.label.RevertLabelUpdateActionHandler;
import com.sitepark.ies.audit.core.service.ReverseActionHandler;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class RevertLabelActionHandler implements ReverseActionHandler {

  private final Map<String, RevertEntityActionHandler> actionHandlers;

  @Inject
  RevertLabelActionHandler(
      RevertLabelCreateActionHandler createHandler,
      RevertLabelUpdateActionHandler updateHandler,
      RevertLabelRemoveActionHandler removeHandler,
      RevertLabelAssignEntitiesActionHandler assignEntitiesHandler,
      RevertLabelUnassignEntitiesActionHandler unassignEntitiesHandler,
      RevertLabelBatchAssignEntitiesActionHandler batchAssignEntitiesHandler,
      RevertLabelBatchUnassignEntitiesActionHandler batchUnassignEntitiesHandler) {
    this.actionHandlers = new HashMap<>();
    this.actionHandlers.put(AuditLogAction.CREATE.name(), createHandler);
    this.actionHandlers.put(AuditLogAction.UPDATE.name(), updateHandler);
    this.actionHandlers.put(AuditLogAction.REMOVE.name(), removeHandler);
    this.actionHandlers.put(AuditLogAction.ASSIGN_ENTITIES_TO_LABEL.name(), assignEntitiesHandler);
    this.actionHandlers.put(
        AuditLogAction.UNASSIGN_ENTITIES_FROM_LABEL.name(), unassignEntitiesHandler);
    this.actionHandlers.put(
        AuditBatchLogAction.BATCH_ASSIGN_ENTITIES_TO_LABEL.name(), batchAssignEntitiesHandler);
    this.actionHandlers.put(
        AuditBatchLogAction.BATCH_UNASSIGN_ENTITIES_FROM_LABEL.name(),
        batchUnassignEntitiesHandler);
  }

  @Override
  public String getEntityType() {
    return EntityRef.toTypeString(Label.class);
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
