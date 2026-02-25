package com.sitepark.ies.application.privilege;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.userrepository.core.usecase.privilege.ReassignRolesToPrivilegesRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = ReassignRolesToPrivilegesServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class ReassignRolesToPrivilegesServiceRequest {

  @NotNull private final ReassignRolesToPrivilegesRequest reassignRolesToPrivilegesRequest;

  @Nullable private final String auditParentId;

  private ReassignRolesToPrivilegesServiceRequest(Builder builder) {
    this.reassignRolesToPrivilegesRequest = builder.reassignRolesToPrivilegesRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public ReassignRolesToPrivilegesRequest reassignRolesToPrivilegesRequest() {
    return this.reassignRolesToPrivilegesRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public boolean isEmpty() {
    return this.reassignRolesToPrivilegesRequest.isEmpty();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.reassignRolesToPrivilegesRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof ReassignRolesToPrivilegesServiceRequest that)
        && Objects.equals(
            this.reassignRolesToPrivilegesRequest, that.reassignRolesToPrivilegesRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "ReassignRolesToUsersServiceRequest{"
        + "reassignRolesToPrivilegesRequest="
        + reassignRolesToPrivilegesRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private ReassignRolesToPrivilegesRequest reassignRolesToPrivilegesRequest;
    private String auditParentId;

    private Builder() {}

    private Builder(ReassignRolesToPrivilegesServiceRequest request) {
      this.reassignRolesToPrivilegesRequest = request.reassignRolesToPrivilegesRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder reassignRolesToPrivilegesRequest(
        ReassignRolesToPrivilegesRequest reassignRolesToPrivilegesRequest) {
      this.reassignRolesToPrivilegesRequest = reassignRolesToPrivilegesRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public ReassignRolesToPrivilegesServiceRequest build() {
      Objects.requireNonNull(
          this.reassignRolesToPrivilegesRequest,
          "reassignRolesToPrivilegesRequest must not be null");
      return new ReassignRolesToPrivilegesServiceRequest(this);
    }
  }
}
