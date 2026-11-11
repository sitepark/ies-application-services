package com.sitepark.ies.application;

import com.sitepark.ies.sharedkernel.domain.EntityRef;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/** Resolves the names of entities of any type through the registered {@link EntityNameResolver}s. */
public final class MultiEntityNameResolver {

  private final Map<String, EntityNameResolver> resolvers;

  @Inject
  @SuppressWarnings("PMD.UseConcurrentHashMap")
  public MultiEntityNameResolver(Set<EntityNameResolver> resolvers) {
    Map<String, EntityNameResolver> byType = new HashMap<>();
    for (EntityNameResolver resolver : resolvers) {
      EntityNameResolver existing = byType.putIfAbsent(resolver.type(), resolver);
      if (existing != null) {
        throw new IllegalArgumentException(
            "Multiple name resolvers registered for entity type "
                + resolver.type()
                + ": "
                + existing.getClass().getName()
                + ", "
                + resolver.getClass().getName());
      }
    }
    this.resolvers = Map.copyOf(byType);
  }

  public @Nullable String resolveName(EntityRef entityRef) {
    return this.resolveNames(Set.of(entityRef)).get(entityRef);
  }

  public Map<EntityRef, String> resolveNames(Set<EntityRef> entityRefs) {
    return entityRefs.stream()
        .collect(Collectors.groupingBy(EntityRef::type, Collectors.toSet()))
        .entrySet()
        .stream()
        .flatMap(entry -> this.resolveNamesOfType(entry.getKey(), entry.getValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private Stream<Map.Entry<EntityRef, String>> resolveNamesOfType(
      String type, Set<EntityRef> entityRefs) {
    EntityNameResolver resolver = this.resolvers.get(type);
    if (resolver == null) {
      return Stream.empty();
    }
    return resolver.resolveNames(entityRefs).entrySet().stream();
  }
}
