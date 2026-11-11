package com.sitepark.ies.application.label;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.usecase.GetLabelsByIdsUseCase;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LabelNameResolverTest {

  private GetLabelsByIdsUseCase getLabelsByIdsUseCase;
  private LabelNameResolver resolver;

  @BeforeEach
  void setUp() {
    this.getLabelsByIdsUseCase = mock();
    this.resolver = new LabelNameResolver(this.getLabelsByIdsUseCase);
  }

  @Test
  void testType() {
    assertEquals("label", this.resolver.type(), "Should resolve labels");
  }

  @Test
  void testResolveNames() {
    Label label = Label.builder().id("1").name("TestLabel").build();
    when(this.getLabelsByIdsUseCase.getLabelsByIds(List.of("1"))).thenReturn(List.of(label));
    EntityRef labelRef = EntityRef.of(Label.class, "1");
    assertEquals(
        Map.of(labelRef, "TestLabel"),
        this.resolver.resolveNames(Set.of(labelRef)),
        "Should return the label name");
  }

  @Test
  void testResolveNamesIgnoresOtherTypes() {
    assertEquals(
        Map.of(),
        this.resolver.resolveNames(Set.of(EntityRef.of("role", "1"))),
        "Should ignore other types");
  }

  @Test
  void testResolveNamesDoesNotLoadOtherTypes() {
    this.resolver.resolveNames(Set.of(EntityRef.of("role", "1")));
    verify(this.getLabelsByIdsUseCase, never()).getLabelsByIds(any());
  }
}
