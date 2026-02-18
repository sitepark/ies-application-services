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

  // privilege assignment operations
  ASSIGN_PRIVILEGES,
  UNASSIGN_PRIVILEGES,

  // role assignment operations
  ASSIGN_ROLES,
  UNASSIGN_ROLES,
}
