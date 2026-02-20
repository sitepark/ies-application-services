package com.sitepark.ies.application.audit.revert;

import static com.sitepark.ies.application.audit.revert.RevertEntityActionHandler.ALL_ENTITIES;

import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.revert.label.RevertLabelBatchAssignEntitiesActionHandler;
import com.sitepark.ies.application.audit.revert.label.RevertLabelBatchUnassignEntitiesActionHandler;
import com.sitepark.ies.audit.core.service.ReverseActionHandler;
import com.sitepark.ies.audit.core.service.RevertRequest;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class RevertMultiEntityActionHandler implements ReverseActionHandler {

  private final Map<String, RevertEntityActionHandler> actionHandlers;

  @Inject
  RevertMultiEntityActionHandler(
      RevertLabelBatchAssignEntitiesActionHandler batchAssignEntitiesHandler,
      RevertLabelBatchUnassignEntitiesActionHandler batchUnassignEntitiesHandler) {
    this.actionHandlers = new HashMap<>();
    this.actionHandlers.put(
        AuditBatchLogAction.BATCH_ASSIGN_LABELS_TO_ENTITIES.name(), batchAssignEntitiesHandler);
    this.actionHandlers.put(
        AuditBatchLogAction.BATCH_UNASSIGN_LABELS_FROM_ENTITIES.name(),
        batchUnassignEntitiesHandler);
  }

  @Override
  public String getEntityType() {
    return ALL_ENTITIES;
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
