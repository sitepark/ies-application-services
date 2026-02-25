package com.sitepark.ies.application.label;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ReassignLabelsToEntitiesServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(ReassignLabelsToEntitiesServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(ReassignLabelsToEntitiesServiceRequest.class).verify();
  }
}
