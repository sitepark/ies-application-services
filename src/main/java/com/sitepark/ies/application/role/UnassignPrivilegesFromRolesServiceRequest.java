package com.sitepark.ies.application.role;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UnassignPrivilegesFromRolesServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UnassignPrivilegesFromRolesServiceRequest {

  @NotNull
  private final com.sitepark.ies.userrepository.core.usecase.role.UnassignPrivilegesFromRolesRequest
      unassignPrivilegesFromRolesRequest;

  @Nullable private final String auditParentId;

  private UnassignPrivilegesFromRolesServiceRequest(Builder builder) {
    this.unassignPrivilegesFromRolesRequest = builder.unassignPrivilegesFromRolesRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public com.sitepark.ies.userrepository.core.usecase.role.UnassignPrivilegesFromRolesRequest
      unassignPrivilegesFromRolesRequest() {
    return this.unassignPrivilegesFromRolesRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public boolean isEmpty() {
    return this.unassignPrivilegesFromRolesRequest.isEmpty();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.unassignPrivilegesFromRolesRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof UnassignPrivilegesFromRolesServiceRequest that)
        && Objects.equals(
            this.unassignPrivilegesFromRolesRequest, that.unassignPrivilegesFromRolesRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "UnassignPrivilegesFromRolesServiceRequest{"
        + "unassignPrivilegesFromRolesRequest="
        + unassignPrivilegesFromRolesRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private com.sitepark.ies.userrepository.core.usecase.role.UnassignPrivilegesFromRolesRequest
        unassignPrivilegesFromRolesRequest;
    private String auditParentId;

    private Builder() {}

    private Builder(UnassignPrivilegesFromRolesServiceRequest request) {
      this.unassignPrivilegesFromRolesRequest = request.unassignPrivilegesFromRolesRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder unassignPrivilegesFromRolesRequest(
        com.sitepark.ies.userrepository.core.usecase.role.UnassignPrivilegesFromRolesRequest
            unassignPrivilegesFromRolesRequest) {
      this.unassignPrivilegesFromRolesRequest = unassignPrivilegesFromRolesRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public UnassignPrivilegesFromRolesServiceRequest build() {
      Objects.requireNonNull(
          this.unassignPrivilegesFromRolesRequest,
          "unassignPrivilegesFromRolesRequest must not be null");
      return new UnassignPrivilegesFromRolesServiceRequest(this);
    }
  }
}
