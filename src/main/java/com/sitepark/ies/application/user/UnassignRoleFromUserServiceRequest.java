package com.sitepark.ies.application.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.userrepository.core.usecase.user.UnassignRolesFromUsersRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UnassignRoleFromUserServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UnassignRoleFromUserServiceRequest {

  @NotNull private final UnassignRolesFromUsersRequest unassignRolesFromUsersRequest;

  @Nullable private final String auditParentId;

  private UnassignRoleFromUserServiceRequest(Builder builder) {
    this.unassignRolesFromUsersRequest = builder.unassignRolesFromUsersRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public UnassignRolesFromUsersRequest unassignRolesFromUsersRequest() {
    return this.unassignRolesFromUsersRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public boolean isEmpty() {
    return this.unassignRolesFromUsersRequest.isEmpty();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.unassignRolesFromUsersRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof UnassignRoleFromUserServiceRequest that)
        && Objects.equals(this.unassignRolesFromUsersRequest, that.unassignRolesFromUsersRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "UnassignRoleFromUserServiceRequest{"
        + "unassignRolesFromUsersRequest="
        + unassignRolesFromUsersRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private UnassignRolesFromUsersRequest unassignRolesFromUsersRequest;
    private String auditParentId;

    private Builder() {}

    private Builder(UnassignRoleFromUserServiceRequest request) {
      this.unassignRolesFromUsersRequest = request.unassignRolesFromUsersRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder unassignRolesFromUsersRequest(
        UnassignRolesFromUsersRequest unassignRolesFromUsersRequest) {
      this.unassignRolesFromUsersRequest = unassignRolesFromUsersRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public UnassignRoleFromUserServiceRequest build() {
      Objects.requireNonNull(
          this.unassignRolesFromUsersRequest, "unassignRolesFromUsersRequest must not be null");
      return new UnassignRoleFromUserServiceRequest(this);
    }
  }
}
