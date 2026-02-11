package com.sitepark.ies.application.audit.revert.role;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.usecase.role.RemoveRoleRequest;
import com.sitepark.ies.userrepository.core.usecase.role.RemoveRoleUseCase;
import jakarta.inject.Inject;

public class RevertRoleCreateActionHandler implements RevertEntityActionHandler {

  private final RemoveRoleUseCase removeRoleUseCase;

  @Inject
  RevertRoleCreateActionHandler(RemoveRoleUseCase removeRoleUseCase) {
    this.removeRoleUseCase = removeRoleUseCase;
  }

  @Override
  public void revert(RevertRequest request) {
    this.removeRoleUseCase.removeRole(
        RemoveRoleRequest.builder()
            .identifier(com.sitepark.ies.sharedkernel.base.Identifier.ofId(request.target().id()))
            .build());
  }
}
