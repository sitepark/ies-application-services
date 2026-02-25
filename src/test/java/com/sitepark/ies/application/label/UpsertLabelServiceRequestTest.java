package com.sitepark.ies.application.label;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UpsertLabelServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(UpsertLabelServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(UpsertLabelServiceRequest.class).verify();
  }
}
