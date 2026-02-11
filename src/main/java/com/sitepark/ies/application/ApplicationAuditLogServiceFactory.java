package com.sitepark.ies.application;

import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.usecase.CreateAuditLogUseCase;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public class ApplicationAuditLogServiceFactory {

  private final CreateAuditLogUseCase createAuditLogUseCase;
  private final AuditLogService auditLogService;
  private final MultiEntityNameResolver multiEntityNameResolver;

  @Inject
  ApplicationAuditLogServiceFactory(
      CreateAuditLogUseCase createAuditLogUseCase,
      AuditLogService auditLogService,
      MultiEntityNameResolver multiEntityNameResolver) {
    this.createAuditLogUseCase = createAuditLogUseCase;
    this.auditLogService = auditLogService;
    this.multiEntityNameResolver = multiEntityNameResolver;
  }

  public ApplicationAuditLogService create(Instant timestamp, @Nullable String parentId) {
    return new ApplicationAuditLogService(
        this.createAuditLogUseCase,
        this.auditLogService,
        this.multiEntityNameResolver,
        timestamp,
        parentId);
  }

  @SuppressWarnings("PMD.UseConcurrentHashMap")
  public Map<String, ApplicationAuditLogService> createForBatchPerType(
      Instant timestamp,
      @Nullable String parentId,
      AuditBatchLogAction action,
      Set<String> entityTypes) {

    ApplicationAuditLogService auditLogService = this.create(timestamp, parentId);

    String parentIdForType =
        entityTypes.size() > 1 ? auditLogService.createBatchLog(null, action) : parentId;
    auditLogService.updateParentId(parentIdForType);

    Map<String, ApplicationAuditLogService> auditLogServicesPerType = new HashMap<>();
    for (String entityType : entityTypes) {
      String auditLogId = auditLogService.createBatchLog(null, action);
      ApplicationAuditLogService auditLogServicePerType = this.create(timestamp, auditLogId);
      auditLogServicesPerType.put(entityType, auditLogServicePerType);
    }

    return auditLogServicesPerType;
  }
}
