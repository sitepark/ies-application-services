package com.sitepark.ies.application.user;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class CreateUserWithPasswordRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(CreateUserWithPasswordRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(CreateUserWithPasswordRequest.class)
        .withIgnoredFields("password")
        .verify();
  }
}
