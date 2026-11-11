package com.sitepark.ies.application.userrepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.user.GetAllUsersUseCase;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserNameResolverTest {

  private GetAllUsersUseCase getAllUsersUseCase;
  private UserNameResolver resolver;

  @BeforeEach
  void setUp() {
    this.getAllUsersUseCase = mock();
    this.resolver = new UserNameResolver(this.getAllUsersUseCase);
  }

  @Test
  void testType() {
    assertEquals("user", this.resolver.type(), "Should resolve users");
  }

  @Test
  void testResolveNames() {
    User user = User.builder().id("1").login("user").lastName("Test").build();
    when(this.getAllUsersUseCase.getAllUsers(any())).thenReturn(List.of(user));
    EntityRef userRef = EntityRef.of(User.class, "1");
    assertEquals(
        Map.of(userRef, user.toDisplayName()),
        this.resolver.resolveNames(Set.of(userRef)),
        "Should return the display name of the user");
  }

  @Test
  void testResolveNamesIgnoresOtherTypes() {
    assertEquals(
        Map.of(),
        this.resolver.resolveNames(Set.of(EntityRef.of("label", "1"))),
        "Should ignore other types");
  }

  @Test
  void testResolveNamesDoesNotLoadOtherTypes() {
    this.resolver.resolveNames(Set.of(EntityRef.of("label", "1")));
    verify(this.getAllUsersUseCase, never()).getAllUsers(any());
  }
}
