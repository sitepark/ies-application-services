package com.sitepark.ies.application.user;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UpsertUserServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(UpsertUserServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(UpsertUserServiceRequest.class).verify();
  }
}
