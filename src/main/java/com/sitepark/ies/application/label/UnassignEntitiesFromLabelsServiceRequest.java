package com.sitepark.ies.application.label;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.label.core.usecase.UnassignEntitiesFromLabelsRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UnassignEntitiesFromLabelsServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UnassignEntitiesFromLabelsServiceRequest {

  @NotNull private final UnassignEntitiesFromLabelsRequest unassignEntitiesFromLabelsRequest;

  @Nullable private final String auditParentId;

  private UnassignEntitiesFromLabelsServiceRequest(Builder builder) {
    Objects.requireNonNull(
        builder.unassignEntitiesFromLabelsRequest,
        "unassignEntitiesFromLabelsRequest must not be null");
    this.unassignEntitiesFromLabelsRequest = builder.unassignEntitiesFromLabelsRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public UnassignEntitiesFromLabelsRequest unassignEntitiesFromLabelsRequest() {
    return this.unassignEntitiesFromLabelsRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.unassignEntitiesFromLabelsRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof UnassignEntitiesFromLabelsServiceRequest that)
        && Objects.equals(
            this.unassignEntitiesFromLabelsRequest, that.unassignEntitiesFromLabelsRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "AssignEntitiesToLabelsServiceRequest{"
        + "unassignEntitiesFromLabelsRequest="
        + unassignEntitiesFromLabelsRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private UnassignEntitiesFromLabelsRequest unassignEntitiesFromLabelsRequest;

    private String auditParentId;

    private Builder() {}

    private Builder(UnassignEntitiesFromLabelsServiceRequest request) {
      this.unassignEntitiesFromLabelsRequest = request.unassignEntitiesFromLabelsRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder unassignEntitiesFromLabelsRequest(
        UnassignEntitiesFromLabelsRequest unassignEntitiesFromLabelsRequest) {
      this.unassignEntitiesFromLabelsRequest = unassignEntitiesFromLabelsRequest;
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
