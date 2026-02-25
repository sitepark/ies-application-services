package com.sitepark.ies.application.user;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class AssignRolesToUsersServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(AssignRolesToUsersServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(AssignRolesToUsersServiceRequest.class).verify();
  }
}
