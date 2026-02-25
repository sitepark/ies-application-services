package com.sitepark.ies.application.role;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.userrepository.core.usecase.role.ReassignPrivilegesToRolesRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = ReassignPrivilegesToRolesServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class ReassignPrivilegesToRolesServiceRequest {

  @NotNull private final ReassignPrivilegesToRolesRequest reassignPrivilegesToRolesRequest;

  @Nullable private final String auditParentId;

  private ReassignPrivilegesToRolesServiceRequest(Builder builder) {
    this.reassignPrivilegesToRolesRequest = builder.reassignPrivilegesToRolesRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public ReassignPrivilegesToRolesRequest reassignPrivilegesToRolesRequest() {
    return this.reassignPrivilegesToRolesRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public boolean isEmpty() {
    return this.reassignPrivilegesToRolesRequest.isEmpty();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.reassignPrivilegesToRolesRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof ReassignPrivilegesToRolesServiceRequest that)
        && Objects.equals(
            this.reassignPrivilegesToRolesRequest, that.reassignPrivilegesToRolesRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "ReassignRolesToUsersServiceRequest{"
        + "reassignPrivilegesToRolesRequest="
        + reassignPrivilegesToRolesRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private ReassignPrivilegesToRolesRequest reassignPrivilegesToRolesRequest;
    private String auditParentId;

    private Builder() {}

    private Builder(ReassignPrivilegesToRolesServiceRequest request) {
      this.reassignPrivilegesToRolesRequest = request.reassignPrivilegesToRolesRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder reassignPrivilegesToRolesRequest(
        ReassignPrivilegesToRolesRequest reassignPrivilegesToRolesRequest) {
      this.reassignPrivilegesToRolesRequest = reassignPrivilegesToRolesRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public ReassignPrivilegesToRolesServiceRequest build() {
      Objects.requireNonNull(
          this.reassignPrivilegesToRolesRequest,
          "reassignPrivilegesToRolesRequest must not be null");
      return new ReassignPrivilegesToRolesServiceRequest(this);
    }
  }
}
