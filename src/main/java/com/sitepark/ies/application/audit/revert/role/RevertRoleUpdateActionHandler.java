package com.sitepark.ies.application.audit.revert.role;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.role.UpdateRoleService;
import com.sitepark.ies.application.role.UpdateRoleServiceRequest;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import com.sitepark.ies.sharedkernel.patch.PatchService;
import com.sitepark.ies.sharedkernel.patch.PatchServiceFactory;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.port.RoleRepository;
import com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest;
import jakarta.inject.Inject;

public class RevertRoleUpdateActionHandler implements RevertEntityActionHandler {

  private final UpdateRoleService updateRoleService;
  private final PatchService<Role> patchService;
  private final RoleRepository repository;

  @Inject
  RevertRoleUpdateActionHandler(
      UpdateRoleService updateRoleService,
      PatchServiceFactory patchServiceFactory,
      RoleRepository repository) {
    this.updateRoleService = updateRoleService;
    this.patchService = patchServiceFactory.createPatchService(Role.class);
    this.repository = repository;
  }

  @Override
  public void revert(RevertRequest request) {
    PatchDocument patch = this.patchService.parsePatch(request.backwardData());
    Role role =
        this.repository
            .get(request.target().id())
            .orElseThrow(
                () ->
                    new RevertFailedException(request, "Role not found: " + request.target().id()));
    Role patchedRole = this.patchService.applyPatch(role, patch);
    this.updateRoleService.updateRole(
        UpdateRoleServiceRequest.builder()
            .updateRoleRequest(UpdateRoleRequest.builder().role(patchedRole).build())
            .auditParentId(request.parentId())
            .build());
  }
}
