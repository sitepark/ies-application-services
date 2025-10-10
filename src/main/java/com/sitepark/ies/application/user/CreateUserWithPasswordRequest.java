package com.sitepark.ies.application.user;

import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.base.IdentifierListBuilder;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

/**
 * Request object for creating a user with optional password.
 *
 * <p>This request encapsulates all data needed to create a user across multiple bounded contexts
 * (userrepository + security).
 */
public final class CreateUserWithPasswordRequest {

  private final User user;

  @Nullable private final String password;

  private final List<Identifier> roleIdentifiers;

  @Nullable private final String auditParentId;

  private CreateUserWithPasswordRequest(Builder builder) {
    this.user = builder.user;
    this.password = builder.password;
    this.roleIdentifiers = List.copyOf(builder.roleIdentifiers);
    this.auditParentId = builder.auditParentId;
  }

  public User user() {
    return user;
  }

  @Nullable
  public String password() {
    return password;
  }

  public List<Identifier> roleIdentifiers() {
    return roleIdentifiers;
  }

  @Nullable
  public String auditParentId() {
    return auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof CreateUserWithPasswordRequest that)
        && Objects.equals(this.user, that.user)
        && Objects.equals(this.password, that.password)
        && Objects.equals(this.roleIdentifiers, that.roleIdentifiers)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.user, this.password, this.roleIdentifiers, this.auditParentId);
  }

  @Override
  public String toString() {
    return "CreateUserWithPasswordRequest{"
        + "user="
        + user
        + ", password="
        + (password != null ? "***" : null)
        + ", roleIdentifiers="
        + roleIdentifiers
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  public static final class Builder {

    private User user;
    private String password;
    private final List<Identifier> roleIdentifiers = new ArrayList<>();
    private String auditParentId;

    private Builder() {}

    public Builder user(User user) {
      this.user = user;
      return this;
    }

    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public Builder roleIdentifiers(Consumer<IdentifierListBuilder> configurer) {
      IdentifierListBuilder listBuilder = new IdentifierListBuilder();
      configurer.accept(listBuilder);
      this.roleIdentifiers.clear();
      this.roleIdentifiers.addAll(listBuilder.build());
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public CreateUserWithPasswordRequest build() {
      if (this.user == null) {
        throw new IllegalStateException("user is not set");
      }
      return new CreateUserWithPasswordRequest(this);
    }
  }
}
