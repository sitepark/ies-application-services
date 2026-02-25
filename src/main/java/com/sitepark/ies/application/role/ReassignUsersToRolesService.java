package com.sitepark.ies.application.role;

import static java.util.stream.Stream.concat;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.value.ReassignResult;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.role.ReassignUsersToRolesResult;
import com.sitepark.ies.userrepository.core.usecase.role.ReassignUsersToRolesUseCase;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ReassignUsersToRolesService {

  private final ReassignUsersToRolesUseCase reassignUsersToRolesUseCase;
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  ReassignUsersToRolesService(
      ReassignUsersToRolesUseCase reassignUsersToRolesUseCase,
      MultiEntityNameResolver multiEntityNameResolver,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.reassignUsersToRolesUseCase = reassignUsersToRolesUseCase;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.auditLogServiceFactory = auditLogServiceFactory;
  }

  public ReassignResult reassignUsersToRoles(@NotNull ReassignUsersToRolesServiceRequest request) {

    ReassignUsersToRolesResult result =
        this.reassignUsersToRolesUseCase.reassignUsersToRoles(
            request.reassignUsersToRolesRequest());

    if (result instanceof ReassignUsersToRolesResult.Reassigned reassigned) {
      this.createAuditLogs(reassigned, request.auditParentId());
      return new ReassignResult(reassigned.assignments().size(), reassigned.unassignments().size());
    } else {
      return ReassignResult.empty();
    }
  }

  private void createAuditLogs(
      ReassignUsersToRolesResult.Reassigned result, @Nullable String auditParentId) {

    Map<String, String> roleNames = resolveUserDisplayNames(result);

    var unassignments = result.unassignments().toUserRoleAssignment();
    var assignments = result.assignments().toUserRoleAssignment();

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
                  roleNames.get(userId),
                  AuditLogAction.ASSIGN_ROLES_TO_USERS,
                  roleIds,
                  roleIds);
            });

    unassignments
        .roleIds()
        .forEach(
            userId -> {
              List<String> roleIds = unassignments.roleIds(userId);
              auditLogService.createLog(
                  EntityRef.of(User.class, userId),
                  roleNames.get(userId),
                  AuditLogAction.UNASSIGN_ROLES_FROM_USERS,
                  roleIds,
                  roleIds);
            });
  }

  private Map<String, String> resolveUserDisplayNames(
      ReassignUsersToRolesResult.Reassigned result) {
    Set<String> userIds =
        concat(result.assignments().userIds().stream(), result.unassignments().userIds().stream())
            .collect(Collectors.toSet());
    return multiEntityNameResolver.resolveDisplayUserNames(userIds);
  }
}
