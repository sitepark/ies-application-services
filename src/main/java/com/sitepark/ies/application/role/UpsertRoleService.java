package com.sitepark.ies.application.role;

import com.sitepark.ies.application.value.UpsertResult;
import com.sitepark.ies.userrepository.core.usecase.role.UpsertRoleResult;
import com.sitepark.ies.userrepository.core.usecase.role.UpsertRoleUseCase;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

/**
 * Application Service that orchestrates role update operations with cross-cutting concerns.
 *
 * <p>This service coordinates role updates and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete update operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Update role via role-core
 *   <li>Create audit log entries for changes (only if changes were made)
 * </ul>
 */
public final class UpsertRoleService {

  private final UpsertRoleUseCase upsertRoleUseCase;
  private final CreateRoleService createRoleService;
  private final UpdateRoleService updateRoleService;

  @Inject
  UpsertRoleService(
      UpsertRoleUseCase upsertRoleUseCase,
      CreateRoleService createRoleService,
      UpdateRoleService updateRoleService) {
    this.upsertRoleUseCase = upsertRoleUseCase;
    this.createRoleService = createRoleService;
    this.updateRoleService = updateRoleService;
  }

  public UpsertResult upsertRole(@NotNull UpsertRoleServiceRequest request) {

    UpsertRoleResult result = this.upsertRoleUseCase.upsertRole(request.upsertRoleRequest());

    if (result instanceof UpsertRoleResult.Updated updated) {
      if (!updated.updateRoleResult().hasAnyChanges()) {
        return UpsertResult.updated(false);
      }
      this.updateRoleService.createAuditLogs(updated.updateRoleResult(), request.auditParentId());
      return UpsertResult.updated(true);
    } else if (result instanceof UpsertRoleResult.Created created) {
      this.createRoleService.createAuditLogs(created.createRoleResult(), request.auditParentId());
      return UpsertResult.created(created.roleId());
    }

    return UpsertResult.updated(false);
  }
}
