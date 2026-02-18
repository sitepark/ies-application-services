package com.sitepark.ies.application.privilege;

import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesServiceRequest;
import com.sitepark.ies.application.value.UpsertResult;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpsertPrivilegeResult;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpsertPrivilegeUseCase;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

/**
 * Application Service that orchestrates privilege update operations with cross-cutting concerns.
 *
 * <p>This service coordinates privilege updates and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete update operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Update privilege via privilege-core
 *   <li>Create audit log entries for changes (only if changes were made)
 * </ul>
 */
public final class UpsertPrivilegeService {

  private final UpsertPrivilegeUseCase upsertPrivilegeUseCase;
  private final CreatePrivilegeService createPrivilegeService;
  private final UpdatePrivilegeService updatePrivilegeService;
  private final ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;

  @Inject
  UpsertPrivilegeService(
      UpsertPrivilegeUseCase upsertPrivilegeUseCase,
      CreatePrivilegeService createPrivilegeService,
      UpdatePrivilegeService updatePrivilegeService,
      ReassignLabelsToEntitiesService reassignLabelsToEntitiesService) {
    this.upsertPrivilegeUseCase = upsertPrivilegeUseCase;
    this.createPrivilegeService = createPrivilegeService;
    this.updatePrivilegeService = updatePrivilegeService;
    this.reassignLabelsToEntitiesService = reassignLabelsToEntitiesService;
  }

  public UpsertResult upsertPrivilege(@NotNull UpsertPrivilegeServiceRequest request) {

    UpsertPrivilegeResult result =
        this.upsertPrivilegeUseCase.upsertPrivilege(request.upsertPrivilegeRequest());
    this.createAuditLogs(result, request.auditParentId());

    UpsertResult upsertResult = this.createResult(result);

    ReassignLabelsToEntitiesServiceRequest labelRequest =
        ReassignLabelsToEntitiesServiceRequest.builder()
            .reassignLabelsToEntitiesRequest(
                ReassignLabelsToEntitiesRequest.builder()
                    .entityRefs(
                        configure ->
                            configure.set(EntityRef.of(Privilege.class, result.privilegeId())))
                    .labelIdentifiers(
                        configure -> configure.identifiers(request.labelIdentifiers()))
                    .build())
            .auditParentId(request.auditParentId())
            .build();
    this.reassignLabelsToEntitiesService.reassignEntitiesFromLabels(labelRequest);

    return upsertResult;
  }

  private void createAuditLogs(UpsertPrivilegeResult result, String auditParentId) {
    if (result instanceof UpsertPrivilegeResult.Updated updated) {
      if (updated.updatePrivilegeResult().hasAnyChanges()) {
        this.updatePrivilegeService.createAuditLogs(updated.updatePrivilegeResult(), auditParentId);
      }
    } else if (result instanceof UpsertPrivilegeResult.Created created) {
      this.createPrivilegeService.createAuditLogs(created.createPrivilegeResult(), auditParentId);
    }
  }

  private UpsertResult createResult(UpsertPrivilegeResult result) {
    if (result instanceof UpsertPrivilegeResult.Updated updated) {
      if (!updated.updatePrivilegeResult().hasAnyChanges()) {
        return UpsertResult.updated(false);
      }
      return UpsertResult.updated(true);
    } else if (result instanceof UpsertPrivilegeResult.Created created) {
      return UpsertResult.created(created.privilegeId());
    }

    return UpsertResult.updated(false);
  }
}
