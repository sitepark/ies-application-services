package com.sitepark.ies.application.user;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UpdateUserServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(UpdateUserServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(UpdateUserServiceRequest.class).verify();
  }
}
