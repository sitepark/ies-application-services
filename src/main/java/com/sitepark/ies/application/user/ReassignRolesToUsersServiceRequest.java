package com.sitepark.ies.application.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.userrepository.core.usecase.user.ReassignRolesToUsersRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = ReassignRolesToUsersServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class ReassignRolesToUsersServiceRequest {

  @NotNull private final ReassignRolesToUsersRequest reassignRolesToUsersRequest;

  @Nullable private final String auditParentId;

  private ReassignRolesToUsersServiceRequest(Builder builder) {
    this.reassignRolesToUsersRequest = builder.reassignRolesToUsersRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public ReassignRolesToUsersRequest reassignRolesToUsersRequest() {
    return this.reassignRolesToUsersRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public boolean isEmpty() {
    return this.reassignRolesToUsersRequest.isEmpty();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.reassignRolesToUsersRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof ReassignRolesToUsersServiceRequest that)
        && Objects.equals(this.reassignRolesToUsersRequest, that.reassignRolesToUsersRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "UnassignRolesFromUsersServiceRequest{"
        + "reassignRolesToUsersRequest="
        + reassignRolesToUsersRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private ReassignRolesToUsersRequest reassignRolesToUsersRequest;
    private String auditParentId;

    private Builder() {}

    private Builder(ReassignRolesToUsersServiceRequest request) {
      this.reassignRolesToUsersRequest = request.reassignRolesToUsersRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder reassignRolesToUsersRequest(
        ReassignRolesToUsersRequest reassignRolesToUsersRequest) {
      this.reassignRolesToUsersRequest = reassignRolesToUsersRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public ReassignRolesToUsersServiceRequest build() {
      Objects.requireNonNull(
          this.reassignRolesToUsersRequest, "reassignRolesToUsersRequest must not be null");
      return new ReassignRolesToUsersServiceRequest(this);
    }
  }
}
