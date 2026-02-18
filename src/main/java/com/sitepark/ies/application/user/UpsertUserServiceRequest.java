package com.sitepark.ies.application.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.base.IdentifierListBuilder;
import com.sitepark.ies.userrepository.core.usecase.user.UpsertUserRequest;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UpsertUserServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UpsertUserServiceRequest {

  @NotNull private final UpsertUserRequest upsertUserRequest;
  @NotNull private final List<Identifier> labelIdentifiers;
  @Nullable private final String auditParentId;

  private UpsertUserServiceRequest(Builder builder) {
    Objects.requireNonNull(builder.upsertUserRequest, "upsertUserRequest must not be null");
    this.upsertUserRequest = builder.upsertUserRequest;
    this.labelIdentifiers = List.copyOf(builder.labelIdentifiers);
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public UpsertUserRequest upsertUserRequest() {
    return this.upsertUserRequest;
  }

  public List<Identifier> labelIdentifiers() {
    return this.labelIdentifiers;
  }

  public String auditParentId() {
    return this.auditParentId;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.upsertUserRequest, this.labelIdentifiers, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o)
        && (o instanceof UpsertUserServiceRequest that)
        && Objects.equals(this.upsertUserRequest, that.upsertUserRequest)
        && Objects.equals(this.labelIdentifiers, that.labelIdentifiers)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "UpsertUserServiceRequest{"
        + "upsertUserRequest="
        + upsertUserRequest
        + ", labelIdentifiers="
        + labelIdentifiers
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private UpsertUserRequest upsertUserRequest;
    private final Set<Identifier> labelIdentifiers = new TreeSet<>();
    private String auditParentId;

    private Builder() {}

    private Builder(UpsertUserServiceRequest request) {
      this.upsertUserRequest = request.upsertUserRequest;
      this.labelIdentifiers.addAll(request.labelIdentifiers);
      this.auditParentId = request.auditParentId;
    }

    public Builder upsertUserRequest(UpsertUserRequest upsertUserRequest) {
      this.upsertUserRequest = upsertUserRequest;
      return this;
    }

    public Builder labelIdentifiers(Consumer<IdentifierListBuilder> configurer) {
      IdentifierListBuilder listBuilder = new IdentifierListBuilder();
      configurer.accept(listBuilder);
      this.labelIdentifiers.clear();
      this.labelIdentifiers.addAll(listBuilder.build());
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
