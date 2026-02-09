package com.sitepark.ies.application.label;

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
 * Request to remove one or more labels.
 *
 * @param identifiers the identifiers (IDs or anchors) of the labels to remove
 * @param auditParentId optional parent audit log ID for grouping related operations
 */
public record RemoveLabelsRequest(
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "List.copyOf() in canonical constructor ensures immutability")
        @NotNull
        List<Identifier> identifiers,
    @Nullable String auditParentId) {

  public RemoveLabelsRequest {
    identifiers = identifiers != null ? List.copyOf(identifiers) : Collections.emptyList();
  }

  /**
   * Creates a new builder for RemoveLabelsRequest.
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

  /** Builder for RemoveLabelsRequest. */
  public static final class Builder {

    private List<Identifier> identifiers = Collections.emptyList();
    private String auditParentId;

    /**
     * Sets the identifiers for the labels to remove using a configurator.
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
     * Sets the identifiers for the labels to remove.
     *
     * @param identifiers the list of label identifiers
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
     * Builds the RemoveLabelsRequest.
     *
     * @return the request instance
     * @throws NullPointerException if identifiers is null
     */
    public RemoveLabelsRequest build() {
      Objects.requireNonNull(this.identifiers, "identifiers must not be null");
      return new RemoveLabelsRequest(this.identifiers, this.auditParentId);
    }
  }
}
