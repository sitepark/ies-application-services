package com.sitepark.ies.application.user;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UnassignRolesFromUsersServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(UnassignRolesFromUsersServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(UnassignRolesFromUsersServiceRequest.class).verify();
  }
}
