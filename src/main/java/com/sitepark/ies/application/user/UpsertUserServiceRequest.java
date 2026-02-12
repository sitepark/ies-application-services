package com.sitepark.ies.application.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.userrepository.core.usecase.user.UpsertUserRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UpsertUserServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UpsertUserServiceRequest {

  @NotNull private final UpsertUserRequest upsertUserRequest;
  @Nullable private final String auditParentId;

  private UpsertUserServiceRequest(Builder builder) {
    Objects.requireNonNull(builder.upsertUserRequest, "upsertUserRequest must not be null");
    this.upsertUserRequest = builder.upsertUserRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public UpsertUserRequest upsertUserRequest() {
    return this.upsertUserRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.upsertUserRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o)
        && (o instanceof UpsertUserServiceRequest that)
        && Objects.equals(this.upsertUserRequest, that.upsertUserRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "UpsertUserServiceRequest{"
        + "upsertUserRequest="
        + upsertUserRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private UpsertUserRequest upsertUserRequest;

    private String auditParentId;

    private Builder() {}

    private Builder(UpsertUserServiceRequest request) {
      this.auditParentId = request.auditParentId;
    }

    public Builder upsertUserRequest(UpsertUserRequest upsertUserRequest) {
      this.upsertUserRequest = upsertUserRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public UpsertUserServiceRequest build() {
      return new UpsertUserServiceRequest(this);
    }
  }
}
