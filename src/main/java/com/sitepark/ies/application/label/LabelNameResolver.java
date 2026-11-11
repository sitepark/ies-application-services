package com.sitepark.ies.application.label;

import com.sitepark.ies.application.EntityNameResolver;
import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.usecase.GetLabelsByIdsUseCase;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Resolves the names of labels. */
public final class LabelNameResolver implements EntityNameResolver {

  private static final String ENTITY_TYPE = EntityRef.toTypeString(Label.class);

  private final GetLabelsByIdsUseCase getLabelsByIdsUseCase;

  @Inject
  public LabelNameResolver(GetLabelsByIdsUseCase getLabelsByIdsUseCase) {
    this.getLabelsByIdsUseCase = getLabelsByIdsUseCase;
  }

  @Override
  public String type() {
    return ENTITY_TYPE;
  }

  @Override
  public Map<EntityRef, String> resolveNames(Set<EntityRef> entityRefs) {
    List<String> ids =
        entityRefs.stream()
            .filter(entityRef -> ENTITY_TYPE.equals(entityRef.type()))
            .map(EntityRef::id)
            .distinct()
            .toList();
    if (ids.isEmpty()) {
      return Map.of();
    }
    return this.getLabelsByIdsUseCase.getLabelsByIds(ids).stream()
        .collect(
            Collectors.toMap(
                label ->
                    EntityRef.of(
                        ENTITY_TYPE, Objects.requireNonNull(label.id(), "stored label has an id")),
                Label::name));
  }
}
