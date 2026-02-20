package com.sitepark.ies.application.user;

import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesServiceRequest;
import com.sitepark.ies.application.value.UpsertResult;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.user.UpsertUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.UpsertUserUseCase;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

/**
 * Application Service that orchestrates user update operations with cross-cutting concerns.
 *
 * <p>This service coordinates user updates and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete update operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Update user via user-core
 *   <li>Create audit log entries for changes (only if changes were made)
 * </ul>
 */
public final class UpsertUserService {

  private final UpsertUserUseCase upsertUserUseCase;
  private final CreateUserService createUserService;
  private final UpdateUserService updateUserService;
  private final ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;

  @Inject
  UpsertUserService(
      UpsertUserUseCase upsertUserUseCase,
      CreateUserService createUserService,
      UpdateUserService updateUserService,
      ReassignLabelsToEntitiesService reassignLabelsToEntitiesService) {
    this.upsertUserUseCase = upsertUserUseCase;
    this.createUserService = createUserService;
    this.updateUserService = updateUserService;
    this.reassignLabelsToEntitiesService = reassignLabelsToEntitiesService;
  }

  public UpsertResult upsertUser(@NotNull UpsertUserServiceRequest request) {

    UpsertUserResult result = this.upsertUserUseCase.upsertUser(request.upsertUserRequest());
    this.createAuditLogs(result, request.auditParentId());
    UpsertResult upsertResult = this.createResult(result);

    if (request.labelIdentifiers().shouldUpdate()) {
      ReassignLabelsToEntitiesServiceRequest labelRequest =
          ReassignLabelsToEntitiesServiceRequest.builder()
              .reassignLabelsToEntitiesRequest(
                  ReassignLabelsToEntitiesRequest.builder()
                      .entityRefs(
                          configure -> configure.set(EntityRef.of(User.class, result.userId())))
                      .labelIdentifiers(
                          configure -> configure.identifiers(request.labelIdentifiers().getValue()))
                      .build())
              .auditParentId(request.auditParentId())
              .build();
      this.reassignLabelsToEntitiesService.reassignEntitiesFromLabels(labelRequest);
    }

    return upsertResult;
  }

  private void createAuditLogs(UpsertUserResult result, String auditParentId) {
    if (result instanceof UpsertUserResult.Updated updated) {
      if (updated.updateUserResult().hasAnyChanges()) {
        this.updateUserService.createAuditLogs(updated.updateUserResult(), auditParentId);
      }
    } else if (result instanceof UpsertUserResult.Created created) {
      this.createUserService.createAuditLogs(created.createUserResult(), auditParentId);
    }
  }

  private UpsertResult createResult(UpsertUserResult result) {
    if (result instanceof UpsertUserResult.Updated updated) {
      if (!updated.updateUserResult().hasAnyChanges()) {
        return UpsertResult.updated(false);
      }
      return UpsertResult.updated(true);
    } else if (result instanceof UpsertUserResult.Created created) {
      return UpsertResult.created(created.userId());
    }

    return UpsertResult.updated(false);
  }
}
