package com.sitepark.ies.application.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.base.IdentifierListBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = AssignRolesToUsersRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class AssignRolesToUsersRequest {

  @NotNull private final List<Identifier> roleIdentifiers;

  @NotNull private final List<Identifier> userIdentifiers;

  @Nullable private final String auditParentId;

  private AssignRolesToUsersRequest(Builder builder) {
    this.roleIdentifiers = List.copyOf(builder.roleIdentifiers);
    this.userIdentifiers = List.copyOf(builder.userIdentifiers);
    this.auditParentId = builder.auditParentId;
  }

  public boolean isEmpty() {
    return this.roleIdentifiers.isEmpty() || this.userIdentifiers.isEmpty();
  }

  public static Builder builder() {
    return new Builder();
  }

  public List<Identifier> roleIdentifiers() {
    return this.roleIdentifiers;
  }

  public List<Identifier> userIdentifiers() {
    return this.userIdentifiers;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest toUseCaseRequest() {
    return com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest.builder()
        .roleIdentifiers(configurer -> configurer.identifiers(this.roleIdentifiers))
        .userIdentifiers(configurer -> configurer.identifiers(this.userIdentifiers))
        .build();
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.userIdentifiers, this.roleIdentifiers, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof AssignRolesToUsersRequest that)
        && Objects.equals(this.roleIdentifiers, that.roleIdentifiers)
        && Objects.equals(this.userIdentifiers, that.userIdentifiers)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "AssignPrivilegesToRolesRequest{"
        + ", roleIdsIdentifiers="
        + roleIdentifiers
        + "userIdentifiers="
        + userIdentifiers
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private final Set<Identifier> roleIdentifiers = new TreeSet<>();
    private final Set<Identifier> userIdentifiers = new TreeSet<>();
    private String auditParentId;

    private Builder() {}

    private Builder(AssignRolesToUsersRequest request) {
      this.roleIdentifiers.addAll(request.userIdentifiers);
      this.userIdentifiers.addAll(request.userIdentifiers);
      this.auditParentId = request.auditParentId;
    }

    public Builder roleIdentifiers(Consumer<IdentifierListBuilder> configurer) {
      IdentifierListBuilder listBuilder = new IdentifierListBuilder();
      configurer.accept(listBuilder);
      this.roleIdentifiers.clear();
      this.roleIdentifiers.addAll(listBuilder.build());
      return this;
    }

    public Builder userIdentifiers(Consumer<IdentifierListBuilder> configurer) {
      IdentifierListBuilder listBuilder = new IdentifierListBuilder();
      configurer.accept(listBuilder);
      this.userIdentifiers.clear();
      this.userIdentifiers.addAll(listBuilder.build());
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public AssignRolesToUsersRequest build() {
      return new AssignRolesToUsersRequest(this);
    }
  }
}
