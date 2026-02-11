package com.sitepark.ies.application.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = AssignRolesToUsersServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class AssignRolesToUsersServiceRequest {

  @NotNull
  private final com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest
      assignRolesToUsersRequest;

  @Nullable private final String auditParentId;

  private AssignRolesToUsersServiceRequest(Builder builder) {
    this.assignRolesToUsersRequest = builder.assignRolesToUsersRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest
      assignRolesToUsersRequest() {
    return this.assignRolesToUsersRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public boolean isEmpty() {
    return this.assignRolesToUsersRequest.isEmpty();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.assignRolesToUsersRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof AssignRolesToUsersServiceRequest that)
        && Objects.equals(this.assignRolesToUsersRequest, that.assignRolesToUsersRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "AssignRolesToUsersServiceRequest{"
        + "assignRolesToUsersRequest="
        + assignRolesToUsersRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest
        assignRolesToUsersRequest;
    private String auditParentId;

    private Builder() {}

    private Builder(AssignRolesToUsersServiceRequest request) {
      this.assignRolesToUsersRequest = request.assignRolesToUsersRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder assignRolesToUsersRequest(
        com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest
            assignRolesToUsersRequest) {
      this.assignRolesToUsersRequest = assignRolesToUsersRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public AssignRolesToUsersServiceRequest build() {
      Objects.requireNonNull(
          this.assignRolesToUsersRequest, "assignRolesToUsersRequest must not be null");
      return new AssignRolesToUsersServiceRequest(this);
    }
  }
}
