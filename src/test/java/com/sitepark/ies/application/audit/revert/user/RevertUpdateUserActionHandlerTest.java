package com.sitepark.ies.application.audit.revert.user;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.user.UpdateUserService;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import com.sitepark.ies.sharedkernel.patch.PatchService;
import com.sitepark.ies.sharedkernel.patch.PatchServiceFactory;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.port.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertUpdateUserActionHandlerTest {

  private UpdateUserService updateUserService;

  @SuppressWarnings("PMD.SingularField")
  private PatchServiceFactory patchServiceFactory;

  private UserRepository repository;
  private RevertRequest request;

  @SuppressWarnings("unchecked")
  private PatchService<User> patchService;

  private RevertUpdateUserActionHandler handler;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    this.updateUserService = mock();
    this.patchServiceFactory = mock();
    this.repository = mock();
    this.request = mock();
    this.patchService = mock();

    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");
    when(this.request.backwardData()).thenReturn("{}");

    when(this.patchServiceFactory.createPatchService(User.class)).thenReturn(this.patchService);
    when(this.patchService.parsePatch(any())).thenReturn(mock(PatchDocument.class));

    this.handler =
        new RevertUpdateUserActionHandler(
            this.updateUserService, this.patchServiceFactory, this.repository);
  }

  @Test
  void testRevertCallsUpdateUserService() {
    User user = User.builder().id("1").login("testuser").lastName("Test").build();
    when(this.repository.get("1")).thenReturn(Optional.of(user));
    when(this.patchService.applyPatch(any(), any())).thenReturn(user);

    this.handler.revert(this.request);

    verify(this.updateUserService).updateUser(any());
  }

  @Test
  void testRevertWhenUserNotFoundThrowsRevertFailedException() {
    when(this.repository.get("1")).thenReturn(Optional.empty());

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when user is not found in repository");
  }
}
