package com.sitepark.ies.application.audit.revert.label;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.label.UpdateLabelService;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.port.LabelRepository;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import com.sitepark.ies.sharedkernel.patch.PatchService;
import com.sitepark.ies.sharedkernel.patch.PatchServiceFactory;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevertLabelUpdateActionHandlerTest {

  private UpdateLabelService updateLabelService;

  @SuppressWarnings("PMD.SingularField")
  private PatchServiceFactory patchServiceFactory;

  private LabelRepository repository;
  private RevertRequest request;

  @SuppressWarnings("unchecked")
  private PatchService<Label> patchService;

  private RevertLabelUpdateActionHandler handler;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    this.updateLabelService = mock();
    this.patchServiceFactory = mock();
    this.repository = mock();
    this.request = mock();
    this.patchService = mock();

    AuditLogTarget target = mock();
    when(this.request.target()).thenReturn(target);
    when(target.id()).thenReturn("1");
    when(this.request.parentId()).thenReturn("parent-id");
    when(this.request.backwardData()).thenReturn("{}");

    when(this.patchServiceFactory.createPatchService(Label.class)).thenReturn(this.patchService);
    when(this.patchService.parsePatch(any())).thenReturn(mock(PatchDocument.class));

    this.handler =
        new RevertLabelUpdateActionHandler(
            this.updateLabelService, this.patchServiceFactory, this.repository);
  }

  @Test
  void testRevertCallsUpdateLabelService() {
    Label label = mock();
    when(this.repository.get("1")).thenReturn(Optional.of(label));
    when(this.patchService.applyPatch(any(), any())).thenReturn(label);

    this.handler.revert(this.request);

    verify(this.updateLabelService).updateLabel(any());
  }

  @Test
  void testRevertWhenLabelNotFoundThrowsRevertFailedException() {
    when(this.repository.get("1")).thenReturn(Optional.empty());

    assertThrows(
        RevertFailedException.class,
        () -> this.handler.revert(this.request),
        "revert() should throw RevertFailedException when label is not found in repository");
  }
}
