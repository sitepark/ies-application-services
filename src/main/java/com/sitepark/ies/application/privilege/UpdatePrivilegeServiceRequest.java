package com.sitepark.ies.application.privilege;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UpdatePrivilegeServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UpdatePrivilegeServiceRequest {

  @NotNull
  private final com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
      updatePrivilegeRequest;

  @Nullable private final String auditParentId;

  private UpdatePrivilegeServiceRequest(Builder builder) {
    this.updatePrivilegeRequest = builder.updatePrivilegeRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
      updatePrivilegeRequest() {
    return this.updatePrivilegeRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.updatePrivilegeRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof UpdatePrivilegeServiceRequest that)
        && Objects.equals(this.updatePrivilegeRequest, that.updatePrivilegeRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "UpdatePrivilegeServiceRequest{"
        + "updatePrivilegeRequest="
        + updatePrivilegeRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
        updatePrivilegeRequest;
    private String auditParentId;

    private Builder() {}

    private Builder(UpdatePrivilegeServiceRequest request) {
      this.updatePrivilegeRequest = request.updatePrivilegeRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder updatePrivilegeRequest(
        com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
            updatePrivilegeRequest) {
      this.updatePrivilegeRequest = updatePrivilegeRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public UpdatePrivilegeServiceRequest build() {
      Objects.requireNonNull(
          this.updatePrivilegeRequest, "updatePrivilegeRequest must not be null");
      return new UpdatePrivilegeServiceRequest(this);
    }
  }
}
