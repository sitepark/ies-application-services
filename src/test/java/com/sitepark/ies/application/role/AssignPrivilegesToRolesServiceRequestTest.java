package com.sitepark.ies.application.role;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class AssignPrivilegesToRolesServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(AssignPrivilegesToRolesServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(AssignPrivilegesToRolesServiceRequest.class).verify();
  }
}
