package com.sitepark.ies.application.privilege;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = CreatePrivilegeServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class CreatePrivilegeServiceRequest {

  @NotNull
  private final com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
      createPrivilegeRequest;

  @Nullable private final String auditParentId;

  private CreatePrivilegeServiceRequest(Builder builder) {
    this.createPrivilegeRequest = builder.createPrivilegeRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
      createPrivilegeRequest() {
    return this.createPrivilegeRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.createPrivilegeRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof CreatePrivilegeServiceRequest that)
        && Objects.equals(this.createPrivilegeRequest, that.createPrivilegeRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "CreatePrivilegeServiceRequest{"
        + "createPrivilegeRequest="
        + createPrivilegeRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
        createPrivilegeRequest;
    private String auditParentId;

    private Builder() {}

    private Builder(CreatePrivilegeServiceRequest request) {
      this.createPrivilegeRequest = request.createPrivilegeRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder createPrivilegeRequest(
        com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
            createPrivilegeRequest) {
      this.createPrivilegeRequest = createPrivilegeRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public CreatePrivilegeServiceRequest build() {
      Objects.requireNonNull(
          this.createPrivilegeRequest, "createPrivilegeRequest must not be null");
      return new CreatePrivilegeServiceRequest(this);
    }
  }
}
