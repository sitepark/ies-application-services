package com.sitepark.ies.application.audit.revert.privilege;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.usecase.privilege.RemovePrivilegeRequest;
import com.sitepark.ies.userrepository.core.usecase.privilege.RemovePrivilegeUseCase;
import jakarta.inject.Inject;

public class RevertPrivilegeCreateActionHandler implements RevertEntityActionHandler {

  private final RemovePrivilegeUseCase removePrivilegeUseCase;

  @Inject
  RevertPrivilegeCreateActionHandler(RemovePrivilegeUseCase removePrivilegeUseCase) {
    this.removePrivilegeUseCase = removePrivilegeUseCase;
  }

  @Override
  public void revert(RevertRequest request) {
    this.removePrivilegeUseCase.removePrivilege(
        RemovePrivilegeRequest.builder()
            .identifier(com.sitepark.ies.sharedkernel.base.Identifier.ofId(request.target().id()))
            .build());
  }
}
