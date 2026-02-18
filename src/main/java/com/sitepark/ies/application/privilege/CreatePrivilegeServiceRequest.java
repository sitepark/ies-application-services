package com.sitepark.ies.application.privilege;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.application.user.CreateUserServiceRequest.Builder;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.base.IdentifierListBuilder;
import com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = CreatePrivilegeServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class CreatePrivilegeServiceRequest {

  @NotNull private final CreatePrivilegeRequest createPrivilegeRequest;
  @NotNull private final List<Identifier> labelIdentifiers;
  @Nullable private final String auditParentId;

  private CreatePrivilegeServiceRequest(Builder builder) {
    this.createPrivilegeRequest = builder.createPrivilegeRequest;
    this.labelIdentifiers = List.copyOf(builder.labelIdentifiers);
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
      createPrivilegeRequest() {
    return this.createPrivilegeRequest;
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
    return Objects.hash(this.createPrivilegeRequest, this.labelIdentifiers, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof CreatePrivilegeServiceRequest that)
        && Objects.equals(this.createPrivilegeRequest, that.createPrivilegeRequest)
        && Objects.equals(this.labelIdentifiers, that.labelIdentifiers)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "CreatePrivilegeServiceRequest{"
        + "createPrivilegeRequest="
        + createPrivilegeRequest
        + ", labelIdentifiers="
        + labelIdentifiers
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
        createPrivilegeRequest;
    private final Set<Identifier> labelIdentifiers = new TreeSet<>();
    private String auditParentId;

    private Builder() {}

    private Builder(CreatePrivilegeServiceRequest request) {
      this.createPrivilegeRequest = request.createPrivilegeRequest;
      this.labelIdentifiers.addAll(request.labelIdentifiers);
      this.auditParentId = request.auditParentId;
    }

    public Builder createPrivilegeRequest(
        com.sitepark.ies.userrepository.core.usecase.privilege.CreatePrivilegeRequest
            createPrivilegeRequest) {
      this.createPrivilegeRequest = createPrivilegeRequest;
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

    public CreatePrivilegeServiceRequest build() {
      Objects.requireNonNull(
          this.createPrivilegeRequest, "createPrivilegeRequest must not be null");
      return new CreatePrivilegeServiceRequest(this);
    }
  }
}
