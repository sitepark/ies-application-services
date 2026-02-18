package com.sitepark.ies.application.role;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.sitepark.ies.application.user.CreateUserServiceRequest.Builder;
import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.base.IdentifierListBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = CreateRoleServiceRequest.Builder.class)
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
public final class CreateRoleServiceRequest {

  @NotNull
  private final com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest
      createRoleRequest;

  @NotNull private final List<Identifier> labelIdentifiers;
  @Nullable private final String auditParentId;

  private CreateRoleServiceRequest(Builder builder) {
    this.createRoleRequest = builder.createRoleRequest;
    this.labelIdentifiers = List.copyOf(builder.labelIdentifiers);
    this.auditParentId = builder.auditParentId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest createRoleRequest() {
    return this.createRoleRequest;
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
    return Objects.hash(this.createRoleRequest, this.labelIdentifiers, this.auditParentId);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof CreateRoleServiceRequest that)
        && Objects.equals(this.createRoleRequest, that.createRoleRequest)
        && Objects.equals(this.labelIdentifiers, that.labelIdentifiers)
        && Objects.equals(this.auditParentId, that.auditParentId);
  }

  @Override
  public String toString() {
    return "CreateRoleServiceRequest{"
        + "createRoleRequest="
        + createRoleRequest
        + ", labelIdentifiers="
        + labelIdentifiers
        + ", auditParentId='"
        + auditParentId
        + '\''
        + '}';
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static final class Builder {

    private com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest createRoleRequest;
    private final Set<Identifier> labelIdentifiers = new TreeSet<>();
    private String auditParentId;

    private Builder() {}

    private Builder(CreateRoleServiceRequest request) {
      this.createRoleRequest = request.createRoleRequest;
      this.labelIdentifiers.addAll(request.labelIdentifiers);
      this.auditParentId = request.auditParentId;
    }

    public Builder createRoleRequest(
        com.sitepark.ies.userrepository.core.usecase.role.CreateRoleRequest createRoleRequest) {
      this.createRoleRequest = createRoleRequest;
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

    public CreateRoleServiceRequest build() {
      Objects.requireNonNull(this.createRoleRequest, "createRoleRequest must not be null");
      return new CreateRoleServiceRequest(this);
    }
  }
}
