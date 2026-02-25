package com.sitepark.ies.application.label;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class AssignLabelsToEntitiesServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(AssignLabelsToEntitiesServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(AssignLabelsToEntitiesServiceRequest.class).verify();
  }
}
