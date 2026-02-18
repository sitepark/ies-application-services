package com.sitepark.ies.application.privilege;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.base.IdentifierListBuilder;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = UpdatePrivilegeServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class UpdatePrivilegeServiceRequest {

  @NotNull private final UpdatePrivilegeRequest updatePrivilegeRequest;
  @NotNull private final List<Identifier> labelIdentifiers;
  @Nullable private final String auditParentId;

  private UpdatePrivilegeServiceRequest(Builder builder) {
    this.updatePrivilegeRequest = builder.updatePrivilegeRequest;
    this.labelIdentifiers = List.copyOf(builder.labelIdentifiers);
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
      updatePrivilegeRequest() {
    return this.updatePrivilegeRequest;
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
    return Objects.hash(this.updatePrivilegeRequest, this.labelIdentifiers, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof UpdatePrivilegeServiceRequest that)
        && Objects.equals(this.updatePrivilegeRequest, that.updatePrivilegeRequest)
        && Objects.equals(this.labelIdentifiers, that.labelIdentifiers)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "UpdatePrivilegeServiceRequest{"
        + "updatePrivilegeRequest="
        + updatePrivilegeRequest
        + ", labelIdentifiers="
        + labelIdentifiers
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
        updatePrivilegeRequest;
    private final Set<Identifier> labelIdentifiers = new TreeSet<>();
    private String auditParentId;

    private Builder() {}

    private Builder(UpdatePrivilegeServiceRequest request) {
      this.updatePrivilegeRequest = request.updatePrivilegeRequest;
      this.labelIdentifiers.addAll(request.labelIdentifiers);
      this.auditParentId = request.auditParentId;
    }

    public Builder updatePrivilegeRequest(
        com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
            updatePrivilegeRequest) {
      this.updatePrivilegeRequest = updatePrivilegeRequest;
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

    public UpdatePrivilegeServiceRequest build() {
      Objects.requireNonNull(
          this.updatePrivilegeRequest, "updatePrivilegeRequest must not be null");
      return new UpdatePrivilegeServiceRequest(this);
    }
  }
}
