package com.sitepark.ies.application.role;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.userrepository.core.usecase.role.ReassignUsersToRolesRequest;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@JsonDeserialize(builder = ReassignUsersToRolesServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class ReassignUsersToRolesServiceRequest {

  private final ReassignUsersToRolesRequest reassignUsersToRolesRequest;

  @Nullable private final String auditParentId;

  private ReassignUsersToRolesServiceRequest(Builder builder) {
    this.reassignUsersToRolesRequest = builder.reassignUsersToRolesRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public ReassignUsersToRolesRequest reassignUsersToRolesRequest() {
    return this.reassignUsersToRolesRequest;
  }

  public @Nullable String auditParentId() {
    return this.auditParentId;
  }

  public boolean isEmpty() {
    return this.reassignUsersToRolesRequest.isEmpty();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.reassignUsersToRolesRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof ReassignUsersToRolesServiceRequest that)
        && Objects.equals(this.reassignUsersToRolesRequest, that.reassignUsersToRolesRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "ReassignRolesToUsersServiceRequest{"
        + "reassignUsersToRolesRequest="
        + reassignUsersToRolesRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  @SuppressWarnings("NullAway.Init")
  public static final class Builder {

    private ReassignUsersToRolesRequest reassignUsersToRolesRequest;
    private @Nullable String auditParentId;

    private Builder() {}

    private Builder(ReassignUsersToRolesServiceRequest request) {
      this.reassignUsersToRolesRequest = request.reassignUsersToRolesRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder reassignRolesToUsersRequest(
        ReassignUsersToRolesRequest reassignUsersToRolesRequest) {
      this.reassignUsersToRolesRequest = reassignUsersToRolesRequest;
      return this;
    }

    public Builder auditParentId(@Nullable String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public ReassignUsersToRolesServiceRequest build() {
      Objects.requireNonNull(
          this.reassignUsersToRolesRequest, "reassignUsersToRolesRequest must not be null");
      return new ReassignUsersToRolesServiceRequest(this);
    }
  }
}
