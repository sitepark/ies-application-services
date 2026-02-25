package com.sitepark.ies.application.audit.revert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.label.RevertLabelAssignEntitiesActionHandler;
import com.sitepark.ies.application.audit.revert.label.RevertLabelCreateActionHandler;
import com.sitepark.ies.application.audit.revert.label.RevertLabelRemoveActionHandler;
import com.sitepark.ies.application.audit.revert.label.RevertLabelUnassignEntitiesActionHandler;
import com.sitepark.ies.application.audit.revert.label.RevertLabelUpdateActionHandler;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertLabelActionHandlerTest {

  private RevertLabelCreateActionHandler createHandler;
  private RevertLabelUpdateActionHandler updateHandler;
  private RevertLabelRemoveActionHandler removeHandler;
  private RevertLabelAssignEntitiesActionHandler assignEntitiesHandler;
  private RevertLabelUnassignEntitiesActionHandler unassignEntitiesHandler;
  private RevertLabelActionHandler handler;

  @BeforeEach
  void setUp() {
    this.createHandler = mock();
    this.updateHandler = mock();
    this.removeHandler = mock();
    this.assignEntitiesHandler = mock();
    this.unassignEntitiesHandler = mock();
    this.handler =
        new RevertLabelActionHandler(
            this.createHandler,
            this.updateHandler,
            this.removeHandler,
            this.assignEntitiesHandler,
            this.unassignEntitiesHandler);
  }

  @Test
  void testGetEntityTypeReturnsLabelEntityType() {
    assertEquals(
        EntityRef.toTypeString(Label.class),
        this.handler.getEntityType(),
        "getEntityType() should return the entity type string for Label");
  }

  @Test
  void testRevertDelegatesToCreateHandler() {
    RevertRequest request = mock();
    when(request.action()).thenReturn(AuditLogAction.CREATE.name());
    this.handler.revert(request);
    verify(this.createHandler).revert(request);
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

  @Test
  void testGetEntitiesActionHandlersReturnsAssignAndUnassignHandlers() {
    assertEquals(
        2,
        this.handler.getEntitiesActionHandlers().size(),
        "getEntitiesActionHandlers() should return exactly 2 handlers (assign and unassign)");
  }
}
