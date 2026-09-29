package com.sitepark.ies.application.role;

import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.base.IdentifierListBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

/**
 * Request to remove one or more roles.
 *
 * @param identifiers the identifiers (IDs or anchors) of the roles to remove
 * @param auditParentId optional parent audit log ID for grouping related operations
 */
public record RemoveRolesServiceRequest(
    List<Identifier> identifiers, @Nullable String auditParentId) {

  public RemoveRolesServiceRequest {
    identifiers = identifiers != null ? List.copyOf(identifiers) : Collections.emptyList();
  }

  /**
   * Creates a new builder for RemoveRolesServiceRequest.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Checks if this request has no identifiers.
   *
   * @return true if the identifiers list is empty
   */
  public boolean isEmpty() {
    return this.identifiers.isEmpty();
  }

  /** Builder for RemoveRolesServiceRequest. */
  @SuppressWarnings("NullAway.Init")
  public static final class Builder {

    private List<Identifier> identifiers = Collections.emptyList();
    private @Nullable String auditParentId;

    /**
     * Sets the identifiers for the roles to remove using a configurator.
     *
     * @param configurer a consumer that configures the identifier list
     * @return this builder
     */
    public Builder identifiers(Consumer<IdentifierListBuilder> configurer) {
      IdentifierListBuilder listBuilder = new IdentifierListBuilder();
      configurer.accept(listBuilder);
      this.identifiers = listBuilder.build();
      return this;
    }

    /**
     * Sets the identifiers for the roles to remove.
     *
     * @param identifiers the list of role identifiers
     * @return this builder
     */
    public Builder identifiers(List<Identifier> identifiers) {
      this.identifiers = identifiers != null ? List.copyOf(identifiers) : Collections.emptyList();
      return this;
    }

    /**
     * Sets the audit parent ID.
     *
     * @param auditParentId the parent audit log ID for grouping
     * @return this builder
     */
    public Builder auditParentId(@Nullable String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    /**
     * Builds the RemoveRolesServiceRequest.
     *
     * @return the request instance
     * @throws NullPointerException if identifiers is null
     */
    public RemoveRolesServiceRequest build() {
      Objects.requireNonNull(this.identifiers, "identifiers must not be null");
      return new RemoveRolesServiceRequest(this.identifiers, this.auditParentId);
    }
  }
}
