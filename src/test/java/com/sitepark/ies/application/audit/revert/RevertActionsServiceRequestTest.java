package com.sitepark.ies.application.audit.revert;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class RevertActionsServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(RevertActionsServiceRequest.class)
        .suppress(Warning.NULL_FIELDS)
        .verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(RevertActionsServiceRequest.class).verify();
  }
}
