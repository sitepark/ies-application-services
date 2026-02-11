package com.sitepark.ies.application.audit.revert.privilege;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import com.sitepark.ies.sharedkernel.patch.PatchService;
import com.sitepark.ies.sharedkernel.patch.PatchServiceFactory;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.port.PrivilegeRepository;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeUseCase;
import jakarta.inject.Inject;

public class RevertPrivilegeUpdateActionHandler implements RevertEntityActionHandler {

  private final UpdatePrivilegeUseCase updatePrivilegeUseCase;

  private final PatchService<Privilege> patchService;

  private final PrivilegeRepository repository;

  @Inject
  RevertPrivilegeUpdateActionHandler(
      UpdatePrivilegeUseCase updatePrivilegeUseCase,
      PatchServiceFactory patchServiceFactory,
      PrivilegeRepository repository) {
    this.updatePrivilegeUseCase = updatePrivilegeUseCase;
    this.patchService = patchServiceFactory.createPatchService(Privilege.class);
    this.repository = repository;
  }

  @Override
  public void revert(RevertRequest request) {
    PatchDocument patch = this.patchService.parsePatch(request.backwardData());
    Privilege privilege =
        this.repository
            .get(request.target().id())
            .orElseThrow(
                () ->
                    new RevertFailedException(
                        request, "Privilege not found: " + request.target().id()));
    Privilege patchedPrivilege = this.patchService.applyPatch(privilege, patch);
    this.updatePrivilegeUseCase.updatePrivilege(
        UpdatePrivilegeRequest.builder().privilege(patchedPrivilege).build());
  }
}
