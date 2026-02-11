package com.sitepark.ies.application.label;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.label.core.usecase.UpdateLabelRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UpdateLabelServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UpdateLabelServiceRequest {

  @NotNull private final UpdateLabelRequest updateLabelRequest;
  @Nullable private final String auditParentId;

  private UpdateLabelServiceRequest(Builder builder) {
    Objects.requireNonNull(builder.updateLabelRequest, "updateLabelRequest must not be null");
    this.updateLabelRequest = builder.updateLabelRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public UpdateLabelRequest updateLabelRequest() {
    return this.updateLabelRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.updateLabelRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o)
        && (o instanceof UpdateLabelServiceRequest that)
        && Objects.equals(this.updateLabelRequest, that.updateLabelRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "CreateLabelServiceRequest{"
        + "updateLabelRequest="
        + updateLabelRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private UpdateLabelRequest updateLabelRequest;

    private String auditParentId;

    private Builder() {}

    private Builder(UpdateLabelServiceRequest request) {
      this.auditParentId = request.auditParentId;
    }

    public Builder updateLabelRequest(UpdateLabelRequest updateLabelRequest) {
      this.updateLabelRequest = updateLabelRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public UpdateLabelServiceRequest build() {
      return new UpdateLabelServiceRequest(this);
    }
  }
}
