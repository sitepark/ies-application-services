package com.sitepark.ies.application.label;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.label.core.usecase.ReassignLabelsToEntitiesRequest;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@JsonDeserialize(builder = ReassignLabelsToEntitiesServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class ReassignLabelsToEntitiesServiceRequest {

  private final ReassignLabelsToEntitiesRequest reassignLabelsToEntitiesRequest;

  @Nullable private final String auditParentId;

  private ReassignLabelsToEntitiesServiceRequest(Builder builder) {
    this.reassignLabelsToEntitiesRequest = builder.reassignLabelsToEntitiesRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public ReassignLabelsToEntitiesRequest reassignLabelsToEntitiesRequest() {
    return this.reassignLabelsToEntitiesRequest;
  }

  public @Nullable String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.reassignLabelsToEntitiesRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof ReassignLabelsToEntitiesServiceRequest that)
        && Objects.equals(
            this.reassignLabelsToEntitiesRequest, that.reassignLabelsToEntitiesRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "ReassignLabelsToEntitiesServiceRequest{"
        + "reassignLabelsToEntitiesRequest="
        + reassignLabelsToEntitiesRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  @SuppressWarnings("NullAway.Init")
  public static final class Builder {

    private ReassignLabelsToEntitiesRequest reassignLabelsToEntitiesRequest;

    private @Nullable String auditParentId;

    private Builder() {}

    private Builder(ReassignLabelsToEntitiesServiceRequest request) {
      this.reassignLabelsToEntitiesRequest = request.reassignLabelsToEntitiesRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder reassignLabelsToEntitiesRequest(
        ReassignLabelsToEntitiesRequest reassignLabelsToEntitiesRequest) {
      this.reassignLabelsToEntitiesRequest = reassignLabelsToEntitiesRequest;
      return this;
    }

    public Builder auditParentId(@Nullable String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public ReassignLabelsToEntitiesServiceRequest build() {
      return new ReassignLabelsToEntitiesServiceRequest(this);
    }
  }
}
