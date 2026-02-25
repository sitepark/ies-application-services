package com.sitepark.ies.application.role;

import static java.util.stream.Stream.concat;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.value.ReassignResult;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesResult;
import com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesUseCase;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ReassignPrivilegesToRolesService {

  private final ReassignPrivilegesToRolesUseCase reassignPrivilegesToRolesUseCase;
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @Inject
  ReassignPrivilegesToRolesService(
      ReassignPrivilegesToRolesUseCase reassignPrivilegesToRolesUseCase,
      MultiEntityNameResolver multiEntityNameResolver,
      ApplicationAuditLogServiceFactory auditLogServiceFactory) {
    this.reassignPrivilegesToRolesUseCase = reassignPrivilegesToRolesUseCase;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.auditLogServiceFactory = auditLogServiceFactory;
  }

  public ReassignResult reassignPrivilegesToRoles(
      @NotNull ReassignPrivilegesToRolesServiceRequest request) {

    ReassignPrivilegesToRolesResult result =
        this.reassignPrivilegesToRolesUseCase.reassignPrivilegesToRoles(
            request.reassignPrivilegesToRolesRequest());

    if (result instanceof ReassignPrivilegesToRolesResult.Reassigned reassigned) {
      this.createAuditLogs(reassigned, request.auditParentId());
      return new ReassignResult(reassigned.assignments().size(), reassigned.unassignments().size());
    } else {
      return ReassignResult.empty();
    }
  }

  private void createAuditLogs(
      ReassignPrivilegesToRolesResult.Reassigned result, @Nullable String auditParentId) {

    Map<String, String> roleNames = resolveRoleNames(result);

    var unassignments = result.unassignments();
    var assignments = result.assignments();

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(result.timestamp(), auditParentId);

    int totalChanges = assignments.size() + unassignments.size();
    boolean requiresBatchProcessing = totalChanges > 1;
    if (requiresBatchProcessing) {
      String batchId =
          auditLogService.createBatchLog(
              User.class, AuditBatchLogAction.BATCH_REASSIGN_PRIVILEGES_TO_ROLES);
      auditLogService.updateParentId(batchId);
    }

    assignments
        .roleIds()
        .forEach(
            roleId -> {
              List<String> privilegeIds = assignments.privilegeIds(roleId);
              auditLogService.createLog(
                  EntityRef.of(Role.class, roleId),
                  roleNames.get(roleId),
                  AuditLogAction.ASSIGN_PRIVILEGES_TO_ROLES,
                  privilegeIds,
                  privilegeIds);
            });

    unassignments
        .roleIds()
        .forEach(
            roleId -> {
              List<String> privilegeIds = unassignments.privilegeIds(roleId);
              auditLogService.createLog(
                  EntityRef.of(Role.class, roleId),
                  roleNames.get(roleId),
                  AuditLogAction.UNASSIGN_PRIVILEGES_FROM_ROLES,
                  privilegeIds,
                  privilegeIds);
            });
  }

  private Map<String, String> resolveRoleNames(ReassignPrivilegesToRolesResult.Reassigned result) {
    Set<String> roleIds =
        concat(result.assignments().roleIds().stream(), result.unassignments().roleIds().stream())
            .collect(Collectors.toSet());
    return multiEntityNameResolver.resolveRoleNames(roleIds);
  }
}
