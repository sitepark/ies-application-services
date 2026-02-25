package com.sitepark.ies.application.user;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class CreateUserServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(CreateUserServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(CreateUserServiceRequest.class).verify();
  }
}
