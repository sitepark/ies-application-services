package com.sitepark.ies.application.role;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ReassignUsersToRolesServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(ReassignUsersToRolesServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(ReassignUsersToRolesServiceRequest.class).verify();
  }
}
