package com.sitepark.ies.application.role;

import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesServiceRequest;
import com.sitepark.ies.application.value.UpsertResult;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
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
  private final ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;

  @Inject
  UpsertRoleService(
      UpsertRoleUseCase upsertRoleUseCase,
      CreateRoleService createRoleService,
      UpdateRoleService updateRoleService,
      ReassignLabelsToEntitiesService reassignLabelsToEntitiesService) {
    this.upsertRoleUseCase = upsertRoleUseCase;
    this.createRoleService = createRoleService;
    this.updateRoleService = updateRoleService;
    this.reassignLabelsToEntitiesService = reassignLabelsToEntitiesService;
  }

  public UpsertResult upsertRole(@NotNull UpsertRoleServiceRequest request) {

    UpsertRoleResult result = this.upsertRoleUseCase.upsertRole(request.upsertRoleRequest());
    this.createAuditLogs(result, request.auditParentId());

    UpsertResult upsertResult = this.createResult(result);

    if (request.labelIdentifiers().shouldUpdate()) {
      ReassignLabelsToEntitiesServiceRequest labelRequest =
          ReassignLabelsToEntitiesServiceRequest.builder()
              .reassignLabelsToEntitiesRequest(
                  ReassignLabelsToEntitiesRequest.builder()
                      .entityRefs(
                          configure -> configure.set(EntityRef.of(Role.class, result.roleId())))
                      .labelIdentifiers(
                          configure -> configure.identifiers(request.labelIdentifiers().getValue()))
                      .build())
              .auditParentId(request.auditParentId())
              .build();
      this.reassignLabelsToEntitiesService.reassignEntitiesFromLabels(labelRequest);
    }

    return upsertResult;
  }

  private void createAuditLogs(UpsertRoleResult result, String auditParentId) {
    if (result instanceof UpsertRoleResult.Updated updated) {
      if (updated.updateRoleResult().hasAnyChanges()) {
        this.updateRoleService.createAuditLogs(updated.updateRoleResult(), auditParentId);
      }
    } else if (result instanceof UpsertRoleResult.Created created) {
      this.createRoleService.createAuditLogs(created.createRoleResult(), auditParentId);
    }
  }

  private UpsertResult createResult(UpsertRoleResult result) {
    if (result instanceof UpsertRoleResult.Updated updated) {
      if (!updated.updateRoleResult().hasAnyChanges()) {
        return UpsertResult.updated(false);
      }
      return UpsertResult.updated(true);
    } else if (result instanceof UpsertRoleResult.Created created) {
      return UpsertResult.created(created.roleId());
    }
    return UpsertResult.updated(false);
  }
}
