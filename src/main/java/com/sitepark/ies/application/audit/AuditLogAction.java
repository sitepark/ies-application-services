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

  // Labels-Entities assignment operations
  ASSIGN_LABELS_TO_ENTITIES,
  UNASSIGN_LABELS_FROM_ENTITIES,

  // Scopes-Labels assignment operations
  ASSIGN_SCOPES_TO_LABELS,
  UNASSIGN_SCOPES_FROM_LABELS,

  // user assignment operations
  ASSIGN_ROLES_TO_USERS,
  UNASSIGN_ROLES_FROM_USERS,

  // role assignment operations
  ASSIGN_PRIVILEGES_TO_ROLES,
  UNASSIGN_PRIVILEGES_FROM_ROLES,
}
