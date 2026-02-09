package com.sitepark.ies.application.user;

import com.sitepark.ies.sharedkernel.base.Identifier;
import com.sitepark.ies.sharedkernel.base.IdentifierListBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Request to remove one or more users.
 *
 * @param identifiers the identifiers (IDs or anchors) of the users to remove
 * @param auditParentId optional parent audit log ID for grouping related operations
 */
public record RemoveUsersRequest(
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "List.copyOf() in canonical constructor ensures immutability")
        @NotNull
        List<Identifier> identifiers,
    @Nullable String auditParentId) {

  public RemoveUsersRequest {
    identifiers = identifiers != null ? List.copyOf(identifiers) : Collections.emptyList();
  }

  /**
   * Creates a new builder for RemoveUsersRequest.
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

  /** Builder for RemoveUsersRequest. */
  public static final class Builder {

    private List<Identifier> identifiers = Collections.emptyList();
    private String auditParentId;

    /**
     * Sets the identifiers for the users to remove using a configurator.
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
     * Sets the identifiers for the users to remove.
     *
     * @param identifiers the list of user identifiers
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
    public Builder auditParentId(String auditParentId) {
      this.auditParentId = auditParentId;
      return this;
    }

    /**
     * Builds the RemoveUsersRequest.
     *
     * @return the request instance
     * @throws NullPointerException if identifiers is null
     */
    public RemoveUsersRequest build() {
      Objects.requireNonNull(this.identifiers, "identifiers must not be null");
      return new RemoveUsersRequest(this.identifiers, this.auditParentId);
    }
  }
}
