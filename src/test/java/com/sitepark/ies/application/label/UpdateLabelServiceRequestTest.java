package com.sitepark.ies.application.label;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UpdateLabelServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(UpdateLabelServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(UpdateLabelServiceRequest.class).verify();
  }
}
