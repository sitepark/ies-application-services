package com.sitepark.ies.application.privilege;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.application.user.UpdateUserServiceRequest.Builder;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.base.IdentifierListBuilder;
import com.sitepark.ies.sharedkernel.base.Updatable;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpsertPrivilegeRequest;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UpsertPrivilegeServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UpsertPrivilegeServiceRequest {

  @NotNull private final UpsertPrivilegeRequest upsertPrivilegeRequest;
  @NotNull private final Updatable<List<Identifier>> labelIdentifiers;
  @Nullable private final String auditParentId;

  private UpsertPrivilegeServiceRequest(Builder builder) {
    Objects.requireNonNull(
        builder.upsertPrivilegeRequest, "upsertPrivilegeRequest must not be null");
    this.upsertPrivilegeRequest = builder.upsertPrivilegeRequest;
    this.labelIdentifiers =
        builder.labelIdentifiers != null
            ? Updatable.of(List.copyOf(builder.labelIdentifiers))
            : Updatable.unchanged();
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public UpsertPrivilegeRequest upsertPrivilegeRequest() {
    return this.upsertPrivilegeRequest;
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
    return Objects.hash(this.upsertPrivilegeRequest, this.labelIdentifiers, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o)
        && (o instanceof UpsertPrivilegeServiceRequest that)
        && Objects.equals(this.upsertPrivilegeRequest, that.upsertPrivilegeRequest)
        && Objects.equals(this.labelIdentifiers, that.labelIdentifiers)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "UpsertPrivilegeServiceRequest{"
        + "upsertPrivilegeRequest="
        + upsertPrivilegeRequest
        + ", labelIdentifiers="
        + labelIdentifiers
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private UpsertPrivilegeRequest upsertPrivilegeRequest;
    private Set<Identifier> labelIdentifiers;
    private String auditParentId;

    private Builder() {}

    private Builder(UpsertPrivilegeServiceRequest request) {
      this.upsertPrivilegeRequest = request.upsertPrivilegeRequest;
      if (request.labelIdentifiers.shouldUpdate()) {
        this.labelIdentifiers = new TreeSet<>(request.labelIdentifiers.getValue());
      }
      this.auditParentId = request.auditParentId;
    }

    public Builder upsertPrivilegeRequest(UpsertPrivilegeRequest upsertPrivilegeRequest) {
      this.upsertPrivilegeRequest = upsertPrivilegeRequest;
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

    public UpsertPrivilegeServiceRequest build() {
      return new UpsertPrivilegeServiceRequest(this);
    }
  }
}
