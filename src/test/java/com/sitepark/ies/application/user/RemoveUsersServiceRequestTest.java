package com.sitepark.ies.application.user;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class RemoveUsersServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(RemoveUsersServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(RemoveUsersServiceRequest.class).verify();
  }
}
