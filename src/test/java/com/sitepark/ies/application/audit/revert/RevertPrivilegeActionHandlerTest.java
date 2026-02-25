package com.sitepark.ies.application.audit.revert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.privilege.RevertPrivilegeBatchRemoveActionHandler;
import com.sitepark.ies.application.audit.revert.privilege.RevertPrivilegeCreateActionHandler;
import com.sitepark.ies.application.audit.revert.privilege.RevertPrivilegeRemoveActionHandler;
import com.sitepark.ies.application.audit.revert.privilege.RevertPrivilegeUpdateActionHandler;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertPrivilegeActionHandlerTest {

  private RevertPrivilegeCreateActionHandler createHandler;

  @SuppressWarnings("PMD.SingularField")
  private RevertPrivilegeUpdateActionHandler updateHandler;

  @SuppressWarnings("PMD.SingularField")
  private RevertPrivilegeRemoveActionHandler removeHandler;

  @SuppressWarnings("PMD.SingularField")
  private RevertPrivilegeBatchRemoveActionHandler batchRemoveHandler;

  @SuppressWarnings("PMD.SingularField")
  private RevertLabelActionHandler labelActionHandler;

  private RevertPrivilegeActionHandler handler;

  @BeforeEach
  void setUp() {
    this.createHandler = mock();
    this.updateHandler = mock();
    this.removeHandler = mock();
    this.batchRemoveHandler = mock();
    this.labelActionHandler = mock();
    when(this.labelActionHandler.getEntitiesActionHandlers()).thenReturn(Map.of());
    this.handler =
        new RevertPrivilegeActionHandler(
            this.createHandler,
            this.updateHandler,
            this.removeHandler,
            this.batchRemoveHandler,
            this.labelActionHandler);
  }

  @Test
  void testGetEntityTypeReturnsPrivilegeEntityType() {
    assertEquals(
        EntityRef.toTypeString(Privilege.class),
        this.handler.getEntityType(),
        "getEntityType() should return the entity type string for Privilege");
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
}
