package com.sitepark.ies.application.audit.revert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertUserActionHandlerTest {

  private RevertCreateUserActionHandler createHandler;
  private RevertUpdateUserActionHandler updateHandler;
  private RevertRemoveUserActionHandler removeHandler;
  private RevertBatchRemoveUsersActionHandler batchRemoveHandler;
  private RevertAssignRolesToUsersActionHandler assignRolesHandler;
  private RevertUnassignRolesToUsersActionHandler unassignRolesHandler;
  private RevertBatchAssignRolesToUsersActionHandler batchAssignRolesHandler;
  private RevertBatchReassignRolesToUsersActionHandler batchReassignRolesHandler;
  private RevertBatchUnassignRolesToUsersActionHandler batchUnassignRolesHandler;
  private RevertLabelActionHandler labelActionHandler;
  private RevertUserActionHandler handler;

  @BeforeEach
  void setUp() {
    this.createHandler = mock();
    this.updateHandler = mock();
    this.removeHandler = mock();
    this.batchRemoveHandler = mock();
    this.assignRolesHandler = mock();
    this.unassignRolesHandler = mock();
    this.batchAssignRolesHandler = mock();
    this.batchReassignRolesHandler = mock();
    this.batchUnassignRolesHandler = mock();
    this.labelActionHandler = mock();
    when(this.labelActionHandler.getEntitiesActionHandlers()).thenReturn(Map.of());
    this.handler =
        new RevertUserActionHandler(
            this.createHandler,
            this.updateHandler,
            this.removeHandler,
            this.batchRemoveHandler,
            this.assignRolesHandler,
            this.unassignRolesHandler,
            this.batchAssignRolesHandler,
            this.batchReassignRolesHandler,
            this.batchUnassignRolesHandler,
            this.labelActionHandler);
  }

  @Test
  void testGetEntityTypeReturnsUserEntityType() {
    assertEquals(
        EntityRef.toTypeString(User.class),
        this.handler.getEntityType(),
        "getEntityType() should return the entity type string for User");
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
