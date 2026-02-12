package com.sitepark.ies.application.user;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesServiceRequest;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesRequest;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserResult;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserUseCase;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

/**
 * Application Service that orchestrates user creation operations with cross-cutting concerns.
 *
 * <p>This service coordinates user creation and associated cross-cutting concerns like audit
 * logging, allowing controllers to perform complete creation operations without managing these
 * concerns themselves.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Create user via userrepository-core
 *   <li>Create audit log entries for user creation
 *   <li>Create audit log entries for role assignments
 * </ul>
 */
public final class CreateUserService {

  private final CreateUserUseCase createUserUseCase;
  private final ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  CreateUserService(
      CreateUserUseCase createUserUseCase,
      ReassignLabelsToEntitiesService reassignLabelsToEntitiesService,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.createUserUseCase = createUserUseCase;
    this.reassignLabelsToEntitiesService = reassignLabelsToEntitiesService;
    this.auditLogServiceFactory = auditLogServiceFactory;
  }

  /**
   * Creates a new user and creates audit log entries.
   *
   * <p>This method orchestrates:
   *
   * <ol>
   *   <li>User creation (userrepository-core)
   *   <li>Audit log creation (audit-core) - for user creation
   *   <li>Audit log creation (audit-core) - for role assignments if present
   * </ol>
   *
   * @param request the creation request containing user data, role identifiers, and optional audit
   *     parent ID
   * @return the created user ID
   * @throws com.sitepark.ies.sharedkernel.security.AccessDeniedException if user creation is not
   *     allowed
   * @throws com.sitepark.ies.userrepository.core.domain.exception.LoginAlreadyExistsException if
   *     login already exists
   * @throws com.sitepark.ies.sharedkernel.anchor.AnchorAlreadyExistsException if anchor already
   *     exists
   */
  public String createUser(@NotNull CreateUserServiceRequest request) {

    CreateUserResult result = this.createUserUseCase.createUser(request.createUserRequest());

    this.createAuditLogs(result, request.auditParentId());

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

    return result.userId();
  }

  protected void createAuditLogs(CreateUserResult result, String auditParentId) {
    this.createCreationAuditLog(result, auditParentId);
    this.createRoleAssignmentAuditLogs(result, auditParentId);
  }

  private void createCreationAuditLog(CreateUserResult result, String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);
    auditLogService.createLog(
        EntityRef.of(User.class, result.userId()),
        result.snapshot().user().toDisplayName(),
        AuditLogAction.CREATE,
        null,
        result.snapshot());
  }

  private void createRoleAssignmentAuditLogs(CreateUserResult result, String auditParentId) {

    if (!(result.roleAssignmentResult() instanceof AssignRolesToUsersResult.Assigned assigned)) {
      return;
    }

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    var assignments = assigned.assignments();

    String parentId =
        assignments.size() > 1
            ? auditLogService.createBatchLog(User.class, AuditBatchLogAction.BATCH_ASSIGN_ROLES)
            : auditParentId;
    auditLogService.updateParentId(parentId);

    auditLogService.createLog(
        EntityRef.of(User.class, result.snapshot().user().id()),
        result.snapshot().user().toDisplayName(),
        AuditLogAction.ASSIGN_ROLES,
        assignments.roleIds(),
        assignments.roleIds());
  }
}
