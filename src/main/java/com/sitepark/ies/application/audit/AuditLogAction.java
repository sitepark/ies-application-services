package com.sitepark.ies.application.audit;

/**
 * Audit log actions operations.
 *
 * <p>These actions represent the types of operations that can be performed and are used for audit
 * logging purposes.
 */
public enum AuditLogAction {
  CREATE,
  UPDATE,
  REMOVE,
  RESTORE,

  // Label assignment operations
  ASSIGN_ENTITIES_TO_LABEL,
  UNASSIGN_ENTITIES_FROM_LABEL,
  ASSIGN_SCOPES_TO_LABEL,
  UNASSIGN_SCOPES_FROM_LABEL,

  // privilege assignment operations
  ASSIGN_PRIVILEGES,
  UNASSIGN_PRIVILEGES,

  // role assignment operations
  ASSIGN_ROLES,
  UNASSIGN_ROLES,
}
