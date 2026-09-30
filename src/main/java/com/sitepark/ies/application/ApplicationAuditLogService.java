package com.sitepark.ies.application;

import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.audit.core.domain.exception.CreateAuditLogEntryFailedException;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.usecase.CreateAuditLogRequest;
import com.sitepark.ies.audit.core.usecase.CreateAuditLogUseCase;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import java.io.IOException;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class ApplicationAuditLogService {
  private final CreateAuditLogUseCase createAuditLogUseCase;
  private final AuditLogService auditLogService;
  private final MultiEntityNameResolver multiEntityNameResolver;
  private final Instant timestamp;
  private @Nullable String parentId;

  ApplicationAuditLogService(
      CreateAuditLogUseCase createAuditLogUseCase,
      AuditLogService auditLogService,
      MultiEntityNameResolver multiEntityNameResolver,
      Instant timestamp,
      @Nullable String parentId) {
    this.createAuditLogUseCase = createAuditLogUseCase;
    this.auditLogService = auditLogService;
    this.multiEntityNameResolver = multiEntityNameResolver;
    this.timestamp = timestamp;
    this.parentId = parentId;
  }

  public @Nullable String parentId() {
    return this.parentId;
  }

  public void updateParentId(@Nullable String parentId) {
    this.parentId = parentId;
  }

  // audit-core does not annotate the optional values of AuditLogTarget and CreateAuditLogRequest
  // (id, name, data, parent id) as @Nullable yet
  @SuppressWarnings("NullAway")
  public String createBatchLog(@Nullable Class<?> type, AuditBatchLogAction action) {
    // A batch log may span several types; then it has no type (EntityRef.toTypeString rejects null)
    String typeString = type == null ? null : EntityRef.toTypeString(type);
    AuditLogTarget target = new AuditLogTarget(typeString, null, null);
    return this.createAuditLogUseCase.createAuditLog(
        new CreateAuditLogRequest(target, action.name(), null, null, timestamp, parentId));
  }

  public String createLog(
      EntityRef entityRef,
      AuditLogAction action,
      @Nullable Object backwardData,
      @Nullable Object forwardData) {
    return this.createLog(
        entityRef,
        this.multiEntityNameResolver.resolveName(entityRef),
        action,
        backwardData,
        forwardData);
  }

  // audit-core does not annotate the optional values of AuditLogTarget and CreateAuditLogRequest
  // (id, name, data, parent id) as @Nullable yet
  @SuppressWarnings("NullAway")
  public String createLog(
      EntityRef entityRef,
      @Nullable String entityName,
      AuditLogAction action,
      @Nullable Object backwardData,
      @Nullable Object forwardData) {
    AuditLogTarget target = new AuditLogTarget(entityRef.type(), entityRef.id(), entityName);
    return this.createLog(target, action, backwardData, forwardData);
  }

  // audit-core does not annotate the optional values of AuditLogTarget and CreateAuditLogRequest
  // (id, name, data, parent id) as @Nullable yet
  @SuppressWarnings("NullAway")
  public String createLog(
      AuditLogTarget target,
      AuditLogAction action,
      @Nullable Object backwardData,
      @Nullable Object forwardData) {

    return this.createAuditLogUseCase.createAuditLog(
        new CreateAuditLogRequest(
            target,
            action.name(),
            this.serialize(target, backwardData),
            this.serialize(target, forwardData),
            this.timestamp,
            this.parentId));
  }

  private @Nullable String serialize(AuditLogTarget target, @Nullable Object o) {
    if (o == null) {
      return null;
    }
    if (o instanceof String string) {
      return string;
    }
    try {
      return this.auditLogService.serialize(o);
    } catch (IOException e) {
      throw new CreateAuditLogEntryFailedException(target, e);
    }
  }
}
