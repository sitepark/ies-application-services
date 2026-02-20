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
import org.jetbrains.annotations.Nullable;

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

  public String parentId() {
    return this.parentId;
  }

  public void updateParentId(String parentId) {
    this.parentId = parentId;
  }

  public String createBatchLog(Class<?> type, AuditBatchLogAction action) {
    AuditLogTarget target = AuditLogTarget.of(type, null, null);
    return this.createAuditLogUseCase.createAuditLog(
        new CreateAuditLogRequest(target, action.name(), null, null, timestamp, parentId));
  }

  public String createLog(
      EntityRef entityRef, AuditLogAction action, Object backwardData, Object forwardData) {
    AuditLogTarget target = this.toTarget(entityRef);
    return this.createLog(target, action, backwardData, forwardData);
  }

  public String createLog(
      EntityRef entityRef,
      String entityName,
      AuditLogAction action,
      Object backwardData,
      Object forwardData) {
    AuditLogTarget target = new AuditLogTarget(entityRef.type(), entityRef.id(), entityName);
    return this.createLog(target, action, backwardData, forwardData);
  }

  public String createLog(
      AuditLogTarget target, AuditLogAction action, Object backwardData, Object forwardData) {

    return this.createAuditLogUseCase.createAuditLog(
        new CreateAuditLogRequest(
            target,
            action.name(),
            this.serialize(target, backwardData),
            this.serialize(target, forwardData),
            this.timestamp,
            this.parentId));
  }

  private String serialize(AuditLogTarget target, Object o) {
    if (o == null) {
      return null;
    }
    if (o instanceof String) {
      return (String) o;
    }
    try {
      return this.auditLogService.serialize(o);
    } catch (IOException e) {
      throw new CreateAuditLogEntryFailedException(target, e);
    }
  }

  private AuditLogTarget toTarget(EntityRef entityRef) {
    return new AuditLogTarget(
        entityRef.type(), entityRef.id(), this.multiEntityNameResolver.resolveName(entityRef));
  }
}
