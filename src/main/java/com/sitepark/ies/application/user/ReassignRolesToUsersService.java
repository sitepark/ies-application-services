package com.sitepark.ies.application.user;

import static java.util.stream.Stream.concat;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.value.ReassignResult;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.user.ReassignRolesToUsersResult;
import com.sitepark.ies.userrepository.core.usecase.user.ReassignRolesToUsersUseCase;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ReassignRolesToUsersService {

  private final ReassignRolesToUsersUseCase reassignRolesToUsersUseCase;
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  ReassignRolesToUsersService(
      ReassignRolesToUsersUseCase reassignRolesToUsersUseCase,
      MultiEntityNameResolver multiEntityNameResolver,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.reassignRolesToUsersUseCase = reassignRolesToUsersUseCase;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.auditLogServiceFactory = auditLogServiceFactory;
  }

  public ReassignResult reassignRolesToUsers(@NotNull ReassignRolesToUsersServiceRequest request) {

    ReassignRolesToUsersResult result =
        this.reassignRolesToUsersUseCase.reassignRolesToUsers(
            request.reassignRolesToUsersRequest());

    if (result instanceof ReassignRolesToUsersResult.Reassigned reassigned) {
      this.createAuditLogs(reassigned, request.auditParentId());
      return new ReassignResult(reassigned.assignments().size(), reassigned.unassignments().size());
    } else {
      return ReassignResult.empty();
    }
  }

  private void createAuditLogs(
      ReassignRolesToUsersResult.Reassigned result, @Nullable String auditParentId) {

    Map<String, String> userDisplayNames = resolveUserDisplayNames(result);

    var unassignments = result.unassignments();
    var assignments = result.assignments();

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    int totalChanges = assignments.size() + unassignments.size();
    boolean requiresBatchProcessing = totalChanges > 1;
    if (requiresBatchProcessing) {
      String batchId =
          auditLogService.createBatchLog(
              User.class, AuditBatchLogAction.BATCH_REASSIGN_ROLES_TO_USERS);
      auditLogService.updateParentId(batchId);
    }

    assignments
        .userIds()
        .forEach(
            userId -> {
              List<String> roleIds = assignments.roleIds(userId);
              auditLogService.createLog(
                  EntityRef.of(User.class, userId),
                  userDisplayNames.get(userId),
                  AuditLogAction.ASSIGN_ROLES_TO_USERS,
                  roleIds,
                  roleIds);
            });

    unassignments
        .userIds()
        .forEach(
            userId -> {
              List<String> roleIds = unassignments.roleIds(userId);
              auditLogService.createLog(
                  EntityRef.of(User.class, userId),
                  userDisplayNames.get(userId),
                  AuditLogAction.UNASSIGN_ROLES_FROM_USERS,
                  roleIds,
                  roleIds);
            });
  }

  private Map<String, String> resolveUserDisplayNames(
      ReassignRolesToUsersResult.Reassigned result) {
    Set<String> userIds =
        concat(result.assignments().userIds().stream(), result.unassignments().userIds().stream())
            .collect(Collectors.toSet());
    return multiEntityNameResolver.resolveDisplayUserNames(userIds);
  }
}
