package com.sitepark.ies.application.role;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.userrepository.core.usecase.role.UpsertRoleRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UpsertRoleServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UpsertRoleServiceRequest {

  @NotNull private final UpsertRoleRequest upsertRoleRequest;
  @Nullable private final String auditParentId;

  private UpsertRoleServiceRequest(Builder builder) {
    Objects.requireNonNull(builder.upsertRoleRequest, "upsertRoleRequest must not be null");
    this.upsertRoleRequest = builder.upsertRoleRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public UpsertRoleRequest upsertRoleRequest() {
    return this.upsertRoleRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.upsertRoleRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o)
        && (o instanceof UpsertRoleServiceRequest that)
        && Objects.equals(this.upsertRoleRequest, that.upsertRoleRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "UpsertRoleServiceRequest{"
        + "upsertRoleRequest="
        + upsertRoleRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private UpsertRoleRequest upsertRoleRequest;

    private String auditParentId;

    private Builder() {}

    private Builder(UpsertRoleServiceRequest request) {
      this.auditParentId = request.auditParentId;
    }

    public Builder upsertRoleRequest(UpsertRoleRequest upsertRoleRequest) {
      this.upsertRoleRequest = upsertRoleRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public UpsertRoleServiceRequest build() {
      return new UpsertRoleServiceRequest(this);
    }
  }
}
