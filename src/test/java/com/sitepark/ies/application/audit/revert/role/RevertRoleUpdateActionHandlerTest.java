package com.sitepark.ies.application.audit.revert.role;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.role.UpdateRoleService;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import com.sitepark.ies.sharedkernel.patch.PatchService;
import com.sitepark.ies.sharedkernel.patch.PatchServiceFactory;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.port.RoleRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertRoleUpdateActionHandlerTest {

  private UpdateRoleService updateRoleService;
  private PatchServiceFactory patchServiceFactory;
  private RoleRepository repository;
  private RevertRequest request;

  @SuppressWarnings("unchecked")
  private PatchService<Role> patchService;

  private RevertRoleUpdateActionHandler handler;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    this.updateRoleService = mock();
    this.patchServiceFactory = mock();
    this.repository = mock();
    this.request = mock();
    this.patchService = mock();

    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");
    when(this.request.backwardData()).thenReturn("{}");

    when(this.patchServiceFactory.createPatchService(Role.class)).thenReturn(this.patchService);
    when(this.patchService.parsePatch(any())).thenReturn(mock(PatchDocument.class));

    this.handler =
        new RevertRoleUpdateActionHandler(
            this.updateRoleService, this.patchServiceFactory, this.repository);
  }

  @Test
  void testRevertCallsUpdateRoleService() {
    Role role = Role.builder().id("1").name("test-role").build();
    when(this.repository.get("1")).thenReturn(Optional.of(role));
    when(this.patchService.applyPatch(any(), any())).thenReturn(role);

    this.handler.revert(this.request);

    verify(this.updateRoleService).updateRole(any());
  }

  @Test
  void testRevertWhenRoleNotFoundThrowsRevertFailedException() {
    when(this.repository.get("1")).thenReturn(Optional.empty());

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when role is not found in repository");
  }
}
