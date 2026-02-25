package com.sitepark.ies.application.audit.revert.privilege;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.privilege.UpdatePrivilegeService;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import com.sitepark.ies.sharedkernel.patch.PatchService;
import com.sitepark.ies.sharedkernel.patch.PatchServiceFactory;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.port.PrivilegeRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertPrivilegeUpdateActionHandlerTest {

  private UpdatePrivilegeService updatePrivilegeService;
  private PatchServiceFactory patchServiceFactory;
  private PrivilegeRepository repository;
  private RevertRequest request;

  @SuppressWarnings("unchecked")
  private PatchService<Privilege> patchService;

  private RevertPrivilegeUpdateActionHandler handler;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    this.updatePrivilegeService = mock();
    this.patchServiceFactory = mock();
    this.repository = mock();
    this.request = mock();
    this.patchService = mock();

    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");
    when(this.request.backwardData()).thenReturn("{}");

    when(this.patchServiceFactory.createPatchService(Privilege.class))
        .thenReturn(this.patchService);
    when(this.patchService.parsePatch(any())).thenReturn(mock(PatchDocument.class));

    this.handler =
        new RevertPrivilegeUpdateActionHandler(
            this.updatePrivilegeService, this.patchServiceFactory, this.repository);
  }

  @Test
  void testRevertCallsUpdatePrivilegeService() {
    Privilege privilege = mock();
    when(this.repository.get("1")).thenReturn(Optional.of(privilege));
    when(this.patchService.applyPatch(any(), any())).thenReturn(privilege);

    this.handler.revert(this.request);

    verify(this.updatePrivilegeService).updatePrivilege(any());
  }

  @Test
  void testRevertWhenPrivilegeNotFoundThrowsRevertFailedException() {
    when(this.repository.get("1")).thenReturn(Optional.empty());

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when privilege is not found in repository");
  }
}
