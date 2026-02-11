package com.sitepark.ies.application.label;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.label.core.usecase.CreateLabelRequest;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = CreateLabelServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class CreateLabelServiceRequest {

  @NotNull private final CreateLabelRequest createLabelRequest;
  @Nullable private final String auditParentId;

  private CreateLabelServiceRequest(Builder builder) {
    this.createLabelRequest = builder.createLabelRequest;
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public CreateLabelRequest createLabelRequest() {
    return this.createLabelRequest;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.createLabelRequest, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o)
        && (o instanceof CreateLabelServiceRequest that)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "CreateLabelServiceRequest{"
        + "createLabelRequest="
        + createLabelRequest
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private CreateLabelRequest createLabelRequest;

    private String auditParentId;

    private Builder() {}

    private Builder(CreateLabelServiceRequest request) {
      this.auditParentId = request.auditParentId;
    }

    public Builder createLabelRequest(CreateLabelRequest createLabelRequest) {
      this.createLabelRequest = createLabelRequest;
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public CreateLabelServiceRequest build() {
      return new CreateLabelServiceRequest(this);
    }
  }
}
