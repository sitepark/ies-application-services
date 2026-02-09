package com.sitepark.ies.application;

import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.sharedkernel.security.EntityAuthorizationService;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("PMD.UseConcurrentHashMap")
public final class MultiEntityAuthorizationService {

  private final Map<String, EntityAuthorizationService> authorizationServices = new HashMap<>();

  @Inject
  public MultiEntityAuthorizationService(Set<EntityAuthorizationService> authorizationServices) {
    for (EntityAuthorizationService authorizationService : authorizationServices) {
      String type = EntityRef.toTypeString(authorizationService.type());
      if (this.authorizationServices.containsKey(type)) {
        throw new IllegalArgumentException(
            "accessControls already exists: "
                + type
                + " (class: "
                + authorizationService.type().getName()
                + ", already exists: "
                + authorizationServices.stream()
                    .filter(ac -> EntityRef.toTypeString(ac.type()).equals(type))
                    .findFirst()
                    .get()
                    .type()
                    .getName()
                + ")");
      }
      this.authorizationServices.put(type, authorizationService);
    }
  }

  public boolean isCreatable(Class<?> type) {
    return getAuthorizationService(EntityRef.toTypeString(type)).isCreatable();
  }

  public boolean isReadable(EntityRef entityRef) {
    return getAuthorizationService(entityRef.type()).isReadable(entityRef.id());
  }

  public boolean isWritable(EntityRef entityRef) {
    return getAuthorizationService(entityRef.type()).isWritable(entityRef.id());
  }

  public boolean isRemovable(EntityRef entityRef) {
    return getAuthorizationService(entityRef.type()).isRemovable(entityRef.id());
  }

  private EntityAuthorizationService getAuthorizationService(String type) {

    EntityAuthorizationService accessControl = this.authorizationServices.get(type);
    if (accessControl == null) {
      throw new IllegalArgumentException("Unknown type: " + type);
    }
    return accessControl;
  }
}
