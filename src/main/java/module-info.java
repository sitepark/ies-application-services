import org.jspecify.annotations.NullMarked;

/**
 * Application Services module for orchestrating operations across multiple bounded contexts.
 *
 * <p>This module contains application services that coordinate use cases from different core
 * modules (label-core, audit-core, etc.) to implement complex business workflows.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Orchestrate multiple use cases from different bounded contexts
 *   <li>Manage cross-context workflows (e.g., a change together with its audit log entry)
 *   <li>Provide convenient APIs for controllers to avoid code duplication
 * </ul>
 *
 * <p><b>Note:</b> This is the Application Layer in Clean Architecture. It does NOT contain business
 * logic - it only coordinates use cases that already contain the business logic.
 */
@NullMarked
module com.sitepark.ies.application {
  exports com.sitepark.ies.application;
  exports com.sitepark.ies.application.audit;
  exports com.sitepark.ies.application.audit.revert;
  exports com.sitepark.ies.application.audit.revert.label;
  exports com.sitepark.ies.application.label;
  exports com.sitepark.ies.application.value;

  requires static org.jspecify;
  requires com.sitepark.ies.label.core;
  requires com.sitepark.ies.sharedkernel;
  requires jakarta.inject;
  requires org.apache.logging.log4j;
  requires java.desktop;
  requires com.fasterxml.jackson.databind;
  requires com.sitepark.ies.audit.core;
}
