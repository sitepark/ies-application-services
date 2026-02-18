package com.sitepark.ies.application.audit;

/**
 * Audit log actions operations.
 *
 * <p>These actions represent the types of operations that can be performed and are used for audit
 * logging purposes.
 */
public enum AuditBatchLogAction {
  BATCH_REMOVE,
  REVERT_BATCH_REMOVE,
  REVERT_BATCH,

  // Labels-Entities assignment operations
  BATCH_ASSIGN_LABELS_TO_ENTITIES,
  BATCH_REASSIGN_LABELS_TO_ENTITIES,
  BATCH_UNASSIGN_LABELS_FROM_ENTITIES,

  // Scopes-Entities assignment operations
  BATCH_ASSIGN_SCOPES_TO_LABELS,
  BATCH_REASSIGN_SCOPES_TO_LABELS,
  BATCH_UNASSIGN_SCOPES_FROM_LABELS,

  // privilege assignment operations
  BATCH_ASSIGN_PRIVILEGES,
  BATCH_UNASSIGN_PRIVILEGES,
  REVERT_BATCH_ASSIGN_PRIVILEGES,
  REVERT_BATCH_UNASSIGN_PRIVILEGES,

  // role assignment operations
  BATCH_ASSIGN_ROLES,
  BATCH_REASSIGN_ROLES,
  BATCH_UNASSIGN_ROLES,
  REVERT_BATCH_ASSIGN_ROLES,
  REVERT_BATCH_UNASSIGN_ROLES
}
