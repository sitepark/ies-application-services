package com.sitepark.ies.application.label;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.label.core.usecase.AssignLabelsToEntitiesRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = AssignLabelsToEntitiesServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class AssignLabelsToEntitiesServiceRequest {

  @NotNull private final AssignLabelsToEntitiesRequest assignLabelsToEntitiesRequest;

  @Nullable private final String auditParentId;

  private AssignLabelsToEntitiesServiceRequest(Builder builder) {
    Objects.requireNonNull(
        builder.assignLabelsToEntitiesRequest, "assignEntitiesToLabelsRequest must not be null");
    this.assignLabelsToEntitiesRequest = builder.assignLabelsToEntitiesRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public AssignLabelsToEntitiesRequest assignEntitiesToLabelsRequest() {
    return this.assignLabelsToEntitiesRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.assignLabelsToEntitiesRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof AssignLabelsToEntitiesServiceRequest that)
        && Objects.equals(this.assignLabelsToEntitiesRequest, that.assignLabelsToEntitiesRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "AssignLabelsToEntitiesServiceRequest{"
        + "assignEntitiesToLabelsRequest="
        + assignLabelsToEntitiesRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private AssignLabelsToEntitiesRequest assignLabelsToEntitiesRequest;

    private String auditParentId;

    private Builder() {}

    private Builder(AssignLabelsToEntitiesServiceRequest request) {
      this.assignLabelsToEntitiesRequest = request.assignLabelsToEntitiesRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder assignEntitiesToLabelsRequest(
        AssignLabelsToEntitiesRequest assignLabelsToEntitiesRequest) {
      this.assignLabelsToEntitiesRequest = assignLabelsToEntitiesRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public AssignLabelsToEntitiesServiceRequest build() {
      return new AssignLabelsToEntitiesServiceRequest(this);
    }
  }
}
