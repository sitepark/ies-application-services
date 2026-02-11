package com.sitepark.ies.application.audit.revert;

import com.sitepark.ies.audit.core.port.ReversalHandlerRegistry;
import com.sitepark.ies.audit.core.service.ReverseActionHandler;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationReversalHandlerRegistry implements ReversalHandlerRegistry {

  private final Map<String, ReverseActionHandler> handler = new ConcurrentHashMap<>();

  @Inject
  ApplicationReversalHandlerRegistry(Set<ReverseActionHandler> handler) {
    handler.forEach(this::addHandler);
  }

  private void addHandler(ReverseActionHandler handler) {
    if (this.handler.containsKey(handler.getEntityType())) {
      throw new IllegalStateException(
          "Multiple reversal handlers registered for entity type: " + handler.getEntityType());
    }
    this.handler.put(handler.getEntityType(), handler);
    this.handler.put(handler.getEntityType(), handler);
  }

  @Override
  public ReverseActionHandler getHandler(String entityType) {
    ReverseActionHandler h = handler.get(entityType);
    if (h == null) {
      throw new IllegalArgumentException(
          "No reversal handler registered for entity type: " + entityType);
    }

    return h;
  }
}
