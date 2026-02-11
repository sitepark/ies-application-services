package com.sitepark.ies.application.audit.revert;

import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.sharedkernel.domain.DomainException;
import java.io.Serial;

/** The <code>RevertFailedException</code> exception is thrown when a revert operation fails. */
public class RevertFailedException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final RevertRequest request;

  public RevertFailedException(RevertRequest request, String message) {
    super(message);
    this.request = request;
  }

  public RevertFailedException(RevertRequest request, String message, Throwable cause) {
    super(message, cause);
    this.request = request;
  }

  public RevertRequest getRequest() {
    return this.request;
  }
}
