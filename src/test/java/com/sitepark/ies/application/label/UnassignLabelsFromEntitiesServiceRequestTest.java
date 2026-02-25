package com.sitepark.ies.application.label;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UnassignLabelsFromEntitiesServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(UnassignLabelsFromEntitiesServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(UnassignLabelsFromEntitiesServiceRequest.class).verify();
  }
}
