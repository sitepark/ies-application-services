package com.sitepark.ies.application.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.base.IdentifierListBuilder;
import com.sitepark.ies.sharedkernel.base.Updatable;
import com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UpdateUserServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UpdateUserServiceRequest {

  @NotNull private final UpdateUserRequest updateUserRequest;
  @NotNull private final Updatable<List<Identifier>> labelIdentifiers;
  @Nullable private final String auditParentId;

  private UpdateUserServiceRequest(Builder builder) {
    this.updateUserRequest = builder.updateUserRequest;
    this.labelIdentifiers =
        builder.labelIdentifiers != null
            ? Updatable.of(List.copyOf(builder.labelIdentifiers))
            : Updatable.unchanged();
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest updateUserRequest() {
    return this.updateUserRequest;
  }

  public Updatable<List<Identifier>> labelIdentifiers() {
    return this.labelIdentifiers;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.updateUserRequest, this.labelIdentifiers, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof UpdateUserServiceRequest that)
        && Objects.equals(this.updateUserRequest, that.updateUserRequest)
        && Objects.equals(this.labelIdentifiers, that.labelIdentifiers)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "UpdateUserServiceRequest{"
        + "updateUserRequest="
        + updateUserRequest
        + ", labelIdentifiers="
        + labelIdentifiers
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest updateUserRequest;
    private Set<Identifier> labelIdentifiers;
    private String auditParentId;

    private Builder() {}

    private Builder(UpdateUserServiceRequest request) {
      this.updateUserRequest = request.updateUserRequest;
      if (request.labelIdentifiers.shouldUpdate()) {
        this.labelIdentifiers = new TreeSet<>(request.labelIdentifiers.getValue());
      }
      this.auditParentId = request.auditParentId;
    }

    public Builder updateUserRequest(
        com.sitepark.ies.userrepository.core.usecase.user.UpdateUserRequest updateUserRequest) {
      this.updateUserRequest = updateUserRequest;
      return this;
    }

    public Builder labelIdentifiers(Consumer<IdentifierListBuilder> configurer) {
      IdentifierListBuilder listBuilder = new IdentifierListBuilder();
      configurer.accept(listBuilder);
      if (listBuilder.changed()) {
        this.labelIdentifiers = new TreeSet<>();
        this.labelIdentifiers.addAll(listBuilder.build());
      }
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public UpdateUserServiceRequest build() {
      Objects.requireNonNull(this.updateUserRequest, "updateUserRequest must not be null");
      return new UpdateUserServiceRequest(this);
    }
  }
}
