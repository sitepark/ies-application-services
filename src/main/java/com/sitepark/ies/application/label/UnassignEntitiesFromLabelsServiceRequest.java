package com.sitepark.ies.application.label;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.label.core.usecase.UnassignLabelsToEntitiesRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UnassignEntitiesFromLabelsServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UnassignEntitiesFromLabelsServiceRequest {

  @NotNull private final UnassignLabelsToEntitiesRequest unassignLabelsToEntitiesRequest;

  @Nullable private final String auditParentId;

  private UnassignEntitiesFromLabelsServiceRequest(Builder builder) {
    Objects.requireNonNull(
        builder.unassignLabelsToEntitiesRequest,
        "unassignLabelsToEntitiesRequest must not be null");
    this.unassignLabelsToEntitiesRequest = builder.unassignLabelsToEntitiesRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public UnassignLabelsToEntitiesRequest unassignEntitiesFromLabelsRequest() {
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
    return (o instanceof UnassignEntitiesFromLabelsServiceRequest that)
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

    private UnassignLabelsToEntitiesRequest unassignLabelsToEntitiesRequest;

    private String auditParentId;

    private Builder() {}

    private Builder(UnassignEntitiesFromLabelsServiceRequest request) {
      this.unassignLabelsToEntitiesRequest = request.unassignLabelsToEntitiesRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder unassignEntitiesFromLabelsRequest(
        UnassignLabelsToEntitiesRequest unassignLabelsToEntitiesRequest) {
      this.unassignLabelsToEntitiesRequest = unassignLabelsToEntitiesRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public UnassignEntitiesFromLabelsServiceRequest build() {
      return new UnassignEntitiesFromLabelsServiceRequest(this);
    }
  }
}
