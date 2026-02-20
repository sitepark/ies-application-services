package com.sitepark.ies.application.config;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.sitepark.ies.application.audit.revert.ApplicationReversalHandlerRegistry;
import com.sitepark.ies.application.audit.revert.RevertLabelActionHandler;
import com.sitepark.ies.application.audit.revert.RevertMultiEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertPrivilegeActionHandler;
import com.sitepark.ies.application.audit.revert.RevertRoleActionHandler;
import com.sitepark.ies.application.audit.revert.RevertUserActionHandler;
import com.sitepark.ies.audit.core.port.ReversalHandlerRegistry;
import com.sitepark.ies.audit.core.service.ReverseActionHandler;

public class ApplicationServiceModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder<ReverseActionHandler> reverseActionHandler =
        Multibinder.newSetBinder(binder(), ReverseActionHandler.class);
    reverseActionHandler.addBinding().to(RevertLabelActionHandler.class);
    reverseActionHandler.addBinding().to(RevertUserActionHandler.class);
    reverseActionHandler.addBinding().to(RevertRoleActionHandler.class);
    reverseActionHandler.addBinding().to(RevertPrivilegeActionHandler.class);
    reverseActionHandler.addBinding().to(RevertMultiEntityActionHandler.class);

    bind(ReversalHandlerRegistry.class).to(ApplicationReversalHandlerRegistry.class);
  }
}
