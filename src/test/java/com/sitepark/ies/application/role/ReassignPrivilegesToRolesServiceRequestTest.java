package com.sitepark.ies.application.role;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ReassignPrivilegesToRolesServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(ReassignPrivilegesToRolesServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(ReassignPrivilegesToRolesServiceRequest.class).verify();
  }
}
