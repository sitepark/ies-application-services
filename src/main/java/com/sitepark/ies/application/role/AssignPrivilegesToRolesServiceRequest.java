package com.sitepark.ies.application.role;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = AssignPrivilegesToRolesServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class AssignPrivilegesToRolesServiceRequest {

  @NotNull
  private final com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest
      assignPrivilegesToRolesRequest;

  @Nullable private final String auditParentId;

  private AssignPrivilegesToRolesServiceRequest(Builder builder) {
    this.assignPrivilegesToRolesRequest = builder.assignPrivilegesToRolesRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest
      assignPrivilegesToRolesRequest() {
    return this.assignPrivilegesToRolesRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public boolean isEmpty() {
    return this.assignPrivilegesToRolesRequest.isEmpty();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.assignPrivilegesToRolesRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof AssignPrivilegesToRolesServiceRequest that)
        && Objects.equals(this.assignPrivilegesToRolesRequest, that.assignPrivilegesToRolesRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "AssignPrivilegesToRolesServiceRequest{"
        + "assignPrivilegesToRolesRequest="
        + assignPrivilegesToRolesRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest
        assignPrivilegesToRolesRequest;
    private String auditParentId;

    private Builder() {}

    private Builder(AssignPrivilegesToRolesServiceRequest request) {
      this.assignPrivilegesToRolesRequest = request.assignPrivilegesToRolesRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder assignPrivilegesToRolesRequest(
        com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest
            assignPrivilegesToRolesRequest) {
      this.assignPrivilegesToRolesRequest = assignPrivilegesToRolesRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public AssignPrivilegesToRolesServiceRequest build() {
      Objects.requireNonNull(
          this.assignPrivilegesToRolesRequest, "assignPrivilegesToRolesRequest must not be null");
      return new AssignPrivilegesToRolesServiceRequest(this);
    }
  }
}
