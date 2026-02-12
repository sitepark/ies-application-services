package com.sitepark.ies.application.privilege;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpsertPrivilegeRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UpsertPrivilegeServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UpsertPrivilegeServiceRequest {

  @NotNull private final UpsertPrivilegeRequest upsertPrivilegeRequest;
  @Nullable private final String auditParentId;

  private UpsertPrivilegeServiceRequest(Builder builder) {
    Objects.requireNonNull(
        builder.upsertPrivilegeRequest, "upsertPrivilegeRequest must not be null");
    this.upsertPrivilegeRequest = builder.upsertPrivilegeRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public UpsertPrivilegeRequest upsertPrivilegeRequest() {
    return this.upsertPrivilegeRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.upsertPrivilegeRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o)
        && (o instanceof UpsertPrivilegeServiceRequest that)
        && Objects.equals(this.upsertPrivilegeRequest, that.upsertPrivilegeRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "UpsertPrivilegeServiceRequest{"
        + "upsertPrivilegeRequest="
        + upsertPrivilegeRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private UpsertPrivilegeRequest upsertPrivilegeRequest;

    private String auditParentId;

    private Builder() {}

    private Builder(UpsertPrivilegeServiceRequest request) {
      this.auditParentId = request.auditParentId;
    }

    public Builder upsertPrivilegeRequest(UpsertPrivilegeRequest upsertPrivilegeRequest) {
      this.upsertPrivilegeRequest = upsertPrivilegeRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public UpsertPrivilegeServiceRequest build() {
      return new UpsertPrivilegeServiceRequest(this);
    }
  }
}
