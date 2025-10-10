package com.sitepark.ies.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.security.core.usecase.SetUserPasswordRequest;
import com.sitepark.ies.security.core.usecase.SetUserPasswordUseCase;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest;
import com.sitepark.ies.userrepository.core.usecase.user.CreateUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserApplicationServiceTest {

  private CreateUserUseCase createUserUseCase;
  private SetUserPasswordUseCase setUserPasswordUseCase;
  private UserApplicationService service;

  @BeforeEach
  void setUp() {
    this.createUserUseCase = mock(CreateUserUseCase.class);
    this.setUserPasswordUseCase = mock(SetUserPasswordUseCase.class);
    this.service = new UserApplicationService(createUserUseCase, setUserPasswordUseCase);
  }

  @Test
  void testCreateUserWithPasswordReturnsUserId() {

    User user = User.builder().login("testuser").lastName("Test").build();

    when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn("123");

    CreateUserWithPasswordRequest request =
        CreateUserWithPasswordRequest.builder()
            .user(user)
            .password("secret123")
            .roleIdentifiers(b -> b.id("1001"))
            .build();

    String userId = service.createUserWithPassword(request);

    assertEquals("123", userId, "Should return the created user ID");
  }

  @Test
  void testCreateUserWithPasswordCallsCreateUserUseCase() {

    User user = User.builder().login("testuser").lastName("Test").build();

    when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn("123");

    CreateUserWithPasswordRequest request =
        CreateUserWithPasswordRequest.builder()
            .user(user)
            .password("secret123")
            .roleIdentifiers(b -> b.id("1001"))
            .build();

    service.createUserWithPassword(request);

    verify(createUserUseCase).createUser(any(CreateUserRequest.class));
  }

  @Test
  void testCreateUserWithPasswordCallsSetPasswordUseCase() {

    User user = User.builder().login("testuser").lastName("Test").build();

    when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn("123");

    CreateUserWithPasswordRequest request =
        CreateUserWithPasswordRequest.builder()
            .user(user)
            .password("secret123")
            .roleIdentifiers(b -> b.id("1001"))
            .build();

    service.createUserWithPassword(request);

    verify(setUserPasswordUseCase).setUserPassword(any(SetUserPasswordRequest.class));
  }

  @Test
  void testCreateUserWithoutPasswordReturnsUserId() {

    User user = User.builder().login("testuser").lastName("Test").build();

    when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn("456");

    CreateUserWithPasswordRequest request =
        CreateUserWithPasswordRequest.builder().user(user).build();

    String userId = service.createUserWithPassword(request);

    assertEquals("456", userId, "Should return the created user ID");
  }

  @Test
  void testCreateUserWithoutPasswordDoesNotCallSetPassword() {

    User user = User.builder().login("testuser").lastName("Test").build();

    when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn("456");

    CreateUserWithPasswordRequest request =
        CreateUserWithPasswordRequest.builder().user(user).build();

    service.createUserWithPassword(request);

    verify(setUserPasswordUseCase, never()).setUserPassword(any(SetUserPasswordRequest.class));
  }

  @Test
  void testCreateUserWithEmptyPasswordReturnsUserId() {

    User user = User.builder().login("testuser").lastName("Test").build();

    when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn("789");

    CreateUserWithPasswordRequest request =
        CreateUserWithPasswordRequest.builder().user(user).password("").build();

    String userId = service.createUserWithPassword(request);

    assertEquals("789", userId, "Should return the created user ID");
  }

  @Test
  void testCreateUserWithEmptyPasswordDoesNotCallSetPassword() {

    User user = User.builder().login("testuser").lastName("Test").build();

    when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn("789");

    CreateUserWithPasswordRequest request =
        CreateUserWithPasswordRequest.builder().user(user).password("").build();

    service.createUserWithPassword(request);

    verify(setUserPasswordUseCase, never()).setUserPassword(any(SetUserPasswordRequest.class));
  }
}
