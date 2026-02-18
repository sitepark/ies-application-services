package com.sitepark.ies.application.role;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.application.user.UpdateUserServiceRequest.Builder;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.base.IdentifierListBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UpdateRoleServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UpdateRoleServiceRequest {

  @NotNull
  private final com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest
      updateRoleRequest;

  @NotNull private final List<Identifier> labelIdentifiers;
  @Nullable private final String auditParentId;

  private UpdateRoleServiceRequest(Builder builder) {
    this.updateRoleRequest = builder.updateRoleRequest;
    this.labelIdentifiers = List.copyOf(builder.labelIdentifiers);
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest updateRoleRequest() {
    return this.updateRoleRequest;
  }

  public List<Identifier> labelIdentifiers() {
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
    return Objects.hash(this.updateRoleRequest, this.labelIdentifiers, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof UpdateRoleServiceRequest that)
        && Objects.equals(this.updateRoleRequest, that.updateRoleRequest)
        && Objects.equals(this.labelIdentifiers, that.labelIdentifiers)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "UpdateRoleServiceRequest{"
        + "updateRoleRequest="
        + updateRoleRequest
        + ", labelIdentifiers="
        + labelIdentifiers
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest updateRoleRequest;
    private final Set<Identifier> labelIdentifiers = new TreeSet<>();
    private String auditParentId;

    private Builder() {}

    private Builder(UpdateRoleServiceRequest request) {
      this.updateRoleRequest = request.updateRoleRequest;
      this.labelIdentifiers.addAll(request.labelIdentifiers);
      this.auditParentId = request.auditParentId;
    }

    public Builder updateRoleRequest(
        com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest updateRoleRequest) {
      this.updateRoleRequest = updateRoleRequest;
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

    public UpdateRoleServiceRequest build() {
      Objects.requireNonNull(this.updateRoleRequest, "updateRoleRequest must not be null");
      return new UpdateRoleServiceRequest(this);
    }
  }
}
