package com.sitepark.ies.application.label;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.label.core.usecase.UnassignLabelsFromEntitiesRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UnassignLabelsFromEntitiesServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UnassignLabelsFromEntitiesServiceRequest {

  @NotNull private final UnassignLabelsFromEntitiesRequest unassignLabelsToEntitiesRequest;

  @Nullable private final String auditParentId;

  private UnassignLabelsFromEntitiesServiceRequest(Builder builder) {
    Objects.requireNonNull(
        builder.unassignLabelsToEntitiesRequest,
        "unassignLabelsToEntitiesRequest must not be null");
    this.unassignLabelsToEntitiesRequest = builder.unassignLabelsToEntitiesRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public UnassignLabelsFromEntitiesRequest unassignEntitiesFromLabelsRequest() {
    return this.unassignLabelsToEntitiesRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.unassignLabelsToEntitiesRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof UnassignLabelsFromEntitiesServiceRequest that)
        && Objects.equals(
            this.unassignLabelsToEntitiesRequest, that.unassignLabelsToEntitiesRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "AssignLabelsToEntitiesServiceRequest{"
        + "unassignLabelsToEntitiesRequest="
        + unassignLabelsToEntitiesRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private UnassignLabelsFromEntitiesRequest unassignLabelsToEntitiesRequest;

    private String auditParentId;

    private Builder() {}

    private Builder(UnassignLabelsFromEntitiesServiceRequest request) {
      this.unassignLabelsToEntitiesRequest = request.unassignLabelsToEntitiesRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder unassignEntitiesFromLabelsRequest(
        UnassignLabelsFromEntitiesRequest unassignLabelsToEntitiesRequest) {
      this.unassignLabelsToEntitiesRequest = unassignLabelsToEntitiesRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public UnassignLabelsFromEntitiesServiceRequest build() {
      return new UnassignLabelsFromEntitiesServiceRequest(this);
    }
  }
}
