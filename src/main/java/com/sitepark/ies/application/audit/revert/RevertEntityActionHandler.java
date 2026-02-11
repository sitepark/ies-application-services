package com.sitepark.ies.application.audit.revert;

import com.sitepark.ies.audit.core.service.RevertRequest;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface RevertEntityActionHandler {

  void revert(RevertRequest request);
}
