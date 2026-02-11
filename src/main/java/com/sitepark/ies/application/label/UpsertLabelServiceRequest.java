package com.sitepark.ies.application.label;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.label.core.usecase.UpsertLabelRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UpsertLabelServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UpsertLabelServiceRequest {

  @NotNull private final UpsertLabelRequest upsertLabelRequest;
  @Nullable private final String auditParentId;

  private UpsertLabelServiceRequest(Builder builder) {
    Objects.requireNonNull(builder.upsertLabelRequest, "upsertLabelRequest must not be null");
    this.upsertLabelRequest = builder.upsertLabelRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public UpsertLabelRequest upsertLabelRequest() {
    return this.upsertLabelRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.upsertLabelRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o)
        && (o instanceof UpsertLabelServiceRequest that)
        && Objects.equals(this.upsertLabelRequest, that.upsertLabelRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "CreateLabelServiceRequest{"
        + "upsertLabelRequest="
        + upsertLabelRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private UpsertLabelRequest upsertLabelRequest;

    private String auditParentId;

    private Builder() {}

    private Builder(UpsertLabelServiceRequest request) {
      this.auditParentId = request.auditParentId;
    }

    public Builder upsertLabelRequest(UpsertLabelRequest upsertLabelRequest) {
      this.upsertLabelRequest = upsertLabelRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public UpsertLabelServiceRequest build() {
      return new UpsertLabelServiceRequest(this);
    }
  }
}
