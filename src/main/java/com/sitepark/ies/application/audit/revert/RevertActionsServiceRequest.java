package com.sitepark.ies.application.audit.revert;

import java.util.List;

public record RevertActionsServiceRequest(List<String> auditLogIds, String auditParentId) {
  public RevertActionsServiceRequest {
    auditLogIds = List.copyOf(auditLogIds);
  }

  @Override
  public List<String> auditLogIds() {
    return List.copyOf(auditLogIds);
  }
}
