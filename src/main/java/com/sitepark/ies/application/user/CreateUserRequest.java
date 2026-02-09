package com.sitepark.ies.application.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.base.IdentifierListBuilder;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = CreateUserRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "PMD.LawOfDemeter"})
public final class CreateUserRequest {

  @NotNull private final User user;

  @NotNull private final List<Identifier> roleIdentifiers;

  @NotNull private final List<Identifier> labelIdentifiers;

  @Nullable private final String auditParentId;

  private CreateUserRequest(Builder builder) {
    this.user = builder.user;
    this.roleIdentifiers = List.copyOf(builder.roleIdentifiers);
    this.labelIdentifiers = List.copyOf(builder.labelIdentifiers);
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  @NotNull
  public User user() {
    return this.user;
  }

  @NotNull
  public List<Identifier> roleIdentifiers() {
    return this.roleIdentifiers;
  }

  @NotNull
  public List<Identifier> labelIdentifiers() {
    return this.labelIdentifiers;
  }

  @Nullable
  public String auditParentId() {
    return this.auditParentId;
  }

  public com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest toUseCaseRequest() {
    return com.sitepark.ies.userrepository.core.usecase.user.CreateUserRequest.builder()
        .user(user)
        .roleIdentifiers(configurer -> configurer.identifiers(roleIdentifiers))
        .build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.user, this.roleIdentifiers, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof CreateUserRequest that)
        && Objects.equals(this.user, that.user)
        && Objects.equals(this.roleIdentifiers, that.roleIdentifiers)
        && Objects.equals(this.labelIdentifiers, that.labelIdentifiers)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "CreateUserRequest{"
        + "user="
        + user
        + ", roleIdentifiers="
        + roleIdentifiers
        + ", labelIdentifiers="
        + labelIdentifiers
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private User user;
    private final Set<Identifier> roleIdentifiers = new TreeSet<>();
    private final Set<Identifier> labelIdentifiers = new TreeSet<>();
    private String auditParentId;

    private Builder() {}

    private Builder(CreateUserRequest request) {
      this.user = request.user;
      this.roleIdentifiers.addAll(request.roleIdentifiers);
      this.labelIdentifiers.addAll(request.labelIdentifiers);
      this.auditParentId = request.auditParentId;
    }

    public Builder user(User user) {
      this.user = user;
      return this;
    }

    public Builder roleIdentifiers(Consumer<IdentifierListBuilder> configurer) {
      IdentifierListBuilder listBuilder = new IdentifierListBuilder();
      configurer.accept(listBuilder);
      this.roleIdentifiers.clear();
      this.roleIdentifiers.addAll(listBuilder.build());
      return this;
    }

    public Builder labelIdentifiers(Consumer<IdentifierListBuilder> configurer) {
      IdentifierListBuilder listBuilder = new IdentifierListBuilder();
      configurer.accept(listBuilder);
      this.labelIdentifiers.clear();
      this.labelIdentifiers.addAll(listBuilder.build());
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public CreateUserRequest build() {
      Objects.requireNonNull(this.user, "User must not be null");
      return new CreateUserRequest(this);
    }
  }
}
