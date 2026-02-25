package com.sitepark.ies.application.user;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ReassignRolesToUsersServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(ReassignRolesToUsersServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(ReassignRolesToUsersServiceRequest.class).verify();
  }
}
