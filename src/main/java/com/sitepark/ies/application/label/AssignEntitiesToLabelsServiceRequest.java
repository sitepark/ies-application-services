package com.sitepark.ies.application.label;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.label.core.usecase.AssignEntitiesToLabelsRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = AssignEntitiesToLabelsServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class AssignEntitiesToLabelsServiceRequest {

  @NotNull private final AssignEntitiesToLabelsRequest assignEntitiesToLabelsRequest;

  @Nullable private final String auditParentId;

  private AssignEntitiesToLabelsServiceRequest(Builder builder) {
    Objects.requireNonNull(
        builder.assignEntitiesToLabelsRequest, "assignEntitiesToLabelsRequest must not be null");
    this.assignEntitiesToLabelsRequest = builder.assignEntitiesToLabelsRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public AssignEntitiesToLabelsRequest assignEntitiesToLabelsRequest() {
    return this.assignEntitiesToLabelsRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.assignEntitiesToLabelsRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof AssignEntitiesToLabelsServiceRequest that)
        && Objects.equals(this.assignEntitiesToLabelsRequest, that.assignEntitiesToLabelsRequest)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "AssignEntitiesToLabelsServiceRequest{"
        + "assignEntitiesToLabelsRequest="
        + assignEntitiesToLabelsRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private AssignEntitiesToLabelsRequest assignEntitiesToLabelsRequest;

    private String auditParentId;

    private Builder() {}

    private Builder(AssignEntitiesToLabelsServiceRequest request) {
      this.assignEntitiesToLabelsRequest = request.assignEntitiesToLabelsRequest;
      this.auditParentId = request.auditParentId;
    }

    public Builder assignEntitiesToLabelsRequest(
        AssignEntitiesToLabelsRequest assignEntitiesToLabelsRequest) {
      this.assignEntitiesToLabelsRequest = assignEntitiesToLabelsRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public AssignEntitiesToLabelsServiceRequest build() {
      return new AssignEntitiesToLabelsServiceRequest(this);
    }
  }
}
