package com.sitepark.ies.application.label;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class CreateLabelServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(CreateLabelServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(CreateLabelServiceRequest.class).verify();
  }
}
