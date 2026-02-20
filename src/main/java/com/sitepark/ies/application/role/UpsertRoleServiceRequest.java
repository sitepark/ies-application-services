package com.sitepark.ies.application.role;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.base.IdentifierListBuilder;
import com.sitepark.ies.sharedkernel.base.Updatable;
import com.sitepark.ies.userrepository.core.usecase.role.UpsertRoleRequest;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UpsertRoleServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UpsertRoleServiceRequest {

  @NotNull private final UpsertRoleRequest upsertRoleRequest;
  @NotNull private final Updatable<List<Identifier>> labelIdentifiers;
  @Nullable private final String auditParentId;

  private UpsertRoleServiceRequest(Builder builder) {
    Objects.requireNonNull(builder.upsertRoleRequest, "upsertRoleRequest must not be null");
    this.upsertRoleRequest = builder.upsertRoleRequest;
    this.labelIdentifiers =
        builder.labelIdentifiers != null
            ? Updatable.of(List.copyOf(builder.labelIdentifiers))
            : Updatable.unchanged();
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public UpsertRoleRequest upsertRoleRequest() {
    return this.upsertRoleRequest;
  }

  public Updatable<List<Identifier>> labelIdentifiers() {
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
    return Objects.hash(this.upsertRoleRequest, this.labelIdentifiers, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o)
        && (o instanceof UpsertRoleServiceRequest that)
        && Objects.equals(this.upsertRoleRequest, that.upsertRoleRequest)
        && Objects.equals(this.labelIdentifiers, that.labelIdentifiers)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "UpsertRoleServiceRequest{"
        + "upsertRoleRequest="
        + upsertRoleRequest
        + ", labelIdentifiers="
        + labelIdentifiers
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private UpsertRoleRequest upsertRoleRequest;
    private Set<Identifier> labelIdentifiers;
    private String auditParentId;

    private Builder() {}

    private Builder(UpsertRoleServiceRequest request) {
      this.upsertRoleRequest = request.upsertRoleRequest;
      if (request.labelIdentifiers.shouldUpdate()) {
        this.labelIdentifiers = new TreeSet<>(request.labelIdentifiers.getValue());
      }
      this.auditParentId = request.auditParentId;
    }

    public Builder upsertRoleRequest(UpsertRoleRequest upsertRoleRequest) {
      this.upsertRoleRequest = upsertRoleRequest;
      return this;
    }

    public Builder labelIdentifiers(Consumer<IdentifierListBuilder> configurer) {
      IdentifierListBuilder listBuilder = new IdentifierListBuilder();
      configurer.accept(listBuilder);
      if (listBuilder.changed()) {
        this.labelIdentifiers = new TreeSet<>();
        this.labelIdentifiers.addAll(listBuilder.build());
      }
      return this;
    }

    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    public UpsertRoleServiceRequest build() {
      return new UpsertRoleServiceRequest(this);
    }
  }
}
