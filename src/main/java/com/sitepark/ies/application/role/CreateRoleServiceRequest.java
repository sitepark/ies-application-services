package com.sitepark.ies.application.role;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = CreateRoleServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class CreateRoleServiceRequest {

  @NotNull
  private final com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest
      createRoleRequest;

  @Nullable private final String auditParentId;

  private CreateRoleServiceRequest(Builder builder) {
    this.createRoleRequest = builder.createRoleRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest createRoleRequest() {
    return this.createRoleRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.createRoleRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof CreateRoleServiceRequest that)
        && Objects.equals(this.createRoleRequest, that.createRoleRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "CreateRoleServiceRequest{"
        + "createRoleRequest="
        + createRoleRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest createRoleRequest;
    private String auditParentId;

    private Builder() {}

    private Builder(CreateRoleServiceRequest request) {
      this.createRoleRequest = request.createRoleRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder createRoleRequest(
        com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest createRoleRequest) {
      this.createRoleRequest = createRoleRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public CreateRoleServiceRequest build() {
      Objects.requireNonNull(this.createRoleRequest, "createRoleRequest must not be null");
      return new CreateRoleServiceRequest(this);
    }
  }
}
