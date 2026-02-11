package com.sitepark.ies.application.user;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesServiceRequest;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.user.ReassignRolesToUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.UpdateUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.UpdateUserUseCase;
import com.sitepark.ies.userrepository.core.usecase.user.UserUpdateResult;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 *   <li>Update user via userrepository-core
 *   <li>Create audit log entries for changes
 *   <li>Create audit log entries for role assignments
 *   <li>Manage transactional boundaries
 * </ul>
 */
public final class UpdateUserService {

  private static final Logger LOGGER = LogManager.getLogger();

  private final UpdateUserUseCase updateUserUseCase;
  private final ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  UpdateUserService(
      UpdateUserUseCase updateUserUseCase,
      ReassignLabelsToEntitiesService reassignLabelsToEntitiesService,
      MultiEntityNameResolver multiEntityNameResolver,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.updateUserUseCase = updateUserUseCase;
    this.reassignLabelsToEntitiesService = reassignLabelsToEntitiesService;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.auditLogServiceFactory = auditLogServiceFactory;
  }

  /**
   * Updates an existing user and creates an audit log entry.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>User update (userrepository-core)
   *   <li>Audit log creation (audit-core) - only if changes were made
   * </ol>
   *
   * <p>If no changes are detected (user data identical to stored data), the update is skipped and
   * no audit log entry is created.
   *
   * @param request contains user data, role identifiers, and optional audit parent ID
   * @return the user ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if user update is not
   *     allowed
   * @throws com.sitepark.ies.userrepository.core.domain.exception.UserNotFoundException if user
   *     does not exist
   * @throws com.sitepark.ies.userrepository.core.domain.exception.LoginAlreadyExistsException if
   *     login already exists for a different user
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorAlreadyExistsException if anchor already
   *     exists for a different user
   */
  public String updateUser(@NotNull UpdateUserServiceRequest request) {

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Updating user with ID '{}'", request.updateUserRequest().user().id());
    }

    UpdateUserResult result = this.updateUserUseCase.updateUser(request.updateUserRequest());

    this.createAuditLogForUserUpdate(result, request.auditParentId());
    this.createAuditLogsForRoleReassignment(result, request.auditParentId());

    if (!request.labelIdentifiers().isEmpty()) {
      ReassignLabelsToEntitiesServiceRequest labelRequest =
          ReassignLabelsToEntitiesServiceRequest.builder()
              .reassignLabelsToEntitiesRequest(
                  ReassignLabelsToEntitiesRequest.builder()
                      .entityRefs(
                          configure -> configure.set(EntityRef.of(User.class, result.userId())))
                      .labelIdentifiers(
                          configure -> configure.identifiers(request.labelIdentifiers()))
                      .build())
              .auditParentId(request.auditParentId())
              .build();
      this.reassignLabelsToEntitiesService.reassignEntitiesFromLabels(labelRequest);
    }

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Successfully processed user update for '{}'", result.userId());
    }

    return result.userId();
  }

  private void createAuditLogForUserUpdate(UpdateUserResult result, String auditParentId) {

    UserUpdateResult.Updated updated = result.userUpdate();
    if (updated == null) {
      return;
    }

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    auditLogService.createLog(
        EntityRef.of(Label.class, result.userId()),
        updated.displayName(),
        AuditLogAction.UPDATE,
        updated.revertPatch().toJson(),
        updated.patch().toJson());
  }

  private void createAuditLogsForRoleReassignment(UpdateUserResult result, String auditParentId) {
    ReassignRolesToUsersResult.Reassigned reassigned =
        (ReassignRolesToUsersResult.Reassigned) result.roleReassignmentResult();
    if (reassigned == null) {
      return;
    }

    String userDisplayName =
        result.userUpdate() != null
            ? result.userUpdate().displayName()
            : this.multiEntityNameResolver.resolveDisplayUserName(result.userId());

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    var assignments = reassigned.assignments();
    if (!assignments.isEmpty()) {
      auditLogService.createLog(
          EntityRef.of(User.class, result.userId()),
          userDisplayName,
          AuditLogAction.ASSIGN_ROLES,
          assignments,
          assignments);
    }

    var unassignment = reassigned.unassignments();
    if (!unassignment.isEmpty()) {
      auditLogService.createLog(
          EntityRef.of(User.class, result.userId()),
          userDisplayName,
          AuditLogAction.UNASSIGN_ROLES,
          unassignment,
          unassignment);
    }
  }
}
