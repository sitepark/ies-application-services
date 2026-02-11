package com.sitepark.ies.application.role;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UpdateRoleServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UpdateRoleServiceRequest {

  @NotNull
  private final com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest
      updateRoleRequest;

  @Nullable private final String auditParentId;

  private UpdateRoleServiceRequest(Builder builder) {
    this.updateRoleRequest = builder.updateRoleRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest updateRoleRequest() {
    return this.updateRoleRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.updateRoleRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof UpdateRoleServiceRequest that)
        && Objects.equals(this.updateRoleRequest, that.updateRoleRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "UpdateRoleServiceRequest{"
        + "updateRoleRequest="
        + updateRoleRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest updateRoleRequest;
    private String auditParentId;

    private Builder() {}

    private Builder(UpdateRoleServiceRequest request) {
      this.updateRoleRequest = request.updateRoleRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder updateRoleRequest(
        com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest updateRoleRequest) {
      this.updateRoleRequest = updateRoleRequest;
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
