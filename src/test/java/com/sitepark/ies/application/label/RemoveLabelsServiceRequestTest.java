package com.sitepark.ies.application.label;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class RemoveLabelsServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(RemoveLabelsServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(RemoveLabelsServiceRequest.class).verify();
  }
}
