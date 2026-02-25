package com.sitepark.ies.application.audit.revert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.role.RevertAssignPrivilegesToRolesActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertBatchAssignPrivilegesToRolesActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertBatchRemoveRolesActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertBatchUnassignPrivilegesToRolesActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertCreateRoleActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertRemoveRoleActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertRoleUpdateActionHandler;
import com.sitepark.ies.application.audit.revert.role.RevertUnassignPrivilegesFromRolesActionHandler;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertRoleActionHandlerTest {

  private RevertCreateRoleActionHandler createHandler;
  private RevertRoleUpdateActionHandler updateHandler;
  private RevertRemoveRoleActionHandler removeHandler;
  private RevertBatchRemoveRolesActionHandler batchRemoveHandler;
  private RevertAssignPrivilegesToRolesActionHandler assignPrivilegesHandler;
  private RevertUnassignPrivilegesFromRolesActionHandler unassignPrivilegesHandler;
  private RevertBatchAssignPrivilegesToRolesActionHandler batchAssignPrivilegesHandler;
  private RevertBatchUnassignPrivilegesToRolesActionHandler batchUnassignPrivilegesHandler;
  private RevertLabelActionHandler labelActionHandler;
  private RevertRoleActionHandler handler;

  @BeforeEach
  void setUp() {
    this.createHandler = mock();
    this.updateHandler = mock();
    this.removeHandler = mock();
    this.batchRemoveHandler = mock();
    this.assignPrivilegesHandler = mock();
    this.unassignPrivilegesHandler = mock();
    this.batchAssignPrivilegesHandler = mock();
    this.batchUnassignPrivilegesHandler = mock();
    this.labelActionHandler = mock();
    when(this.labelActionHandler.getEntitiesActionHandlers()).thenReturn(Map.of());
    this.handler =
        new RevertRoleActionHandler(
            this.createHandler,
            this.updateHandler,
            this.removeHandler,
            this.batchRemoveHandler,
            this.assignPrivilegesHandler,
            this.unassignPrivilegesHandler,
            this.batchAssignPrivilegesHandler,
            this.batchUnassignPrivilegesHandler,
            this.labelActionHandler);
  }

  @Test
  void testGetEntityTypeReturnsRoleEntityType() {
    assertEquals(
        EntityRef.toTypeString(Role.class),
        this.handler.getEntityType(),
        "getEntityType() should return the entity type string for Role");
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
