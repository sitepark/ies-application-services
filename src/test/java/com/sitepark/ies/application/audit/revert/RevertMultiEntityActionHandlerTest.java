package com.sitepark.ies.application.audit.revert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.revert.label.RevertLabelBatchAssignEntitiesActionHandler;
import com.sitepark.ies.application.audit.revert.label.RevertLabelBatchReassignEntitiesActionHandler;
import com.sitepark.ies.application.audit.revert.label.RevertLabelBatchUnassignEntitiesActionHandler;
import com.sitepark.ies.audit.core.service.RevertRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertMultiEntityActionHandlerTest {

  private RevertLabelBatchAssignEntitiesActionHandler batchAssignEntitiesHandler;
  private RevertLabelBatchReassignEntitiesActionHandler batchReassignEntitiesHandler;
  private RevertLabelBatchUnassignEntitiesActionHandler batchUnassignEntitiesHandler;
  private RevertMultiEntityActionHandler handler;

  @BeforeEach
  void setUp() {
    this.batchAssignEntitiesHandler = mock();
    this.batchReassignEntitiesHandler = mock();
    this.batchUnassignEntitiesHandler = mock();
    this.handler =
        new RevertMultiEntityActionHandler(
            this.batchAssignEntitiesHandler,
            this.batchReassignEntitiesHandler,
            this.batchUnassignEntitiesHandler);
  }

  @Test
  void testGetEntityTypeReturnsAllEntities() {
    assertEquals(
        RevertEntityActionHandler.ALL_ENTITIES,
        this.handler.getEntityType(),
        "getEntityType() should return ALL_ENTITIES (\"*\") for the multi-entity handler");
  }

  @Test
  void testRevertDelegatesToBatchAssignEntitiesHandler() {
    RevertRequest request = mock();
    when(request.action()).thenReturn(AuditBatchLogAction.BATCH_ASSIGN_LABELS_TO_ENTITIES.name());
    this.handler.revert(request);
    verify(this.batchAssignEntitiesHandler).revert(request);
  }

  @Test
  void testRevertWithUnknownActionThrowsIllegalArgumentException() {
    RevertRequest request = mock();
    when(request.action()).thenReturn("UNKNOWN_ACTION");
    assertThrows(
        IllegalArgumentException.class,
        () -> this.handler.revert(request),
        "revert() with an unknown action should throw IllegalArgumentException");
  }
}
