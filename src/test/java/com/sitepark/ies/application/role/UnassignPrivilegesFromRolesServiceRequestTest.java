package com.sitepark.ies.application.role;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UnassignPrivilegesFromRolesServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(UnassignPrivilegesFromRolesServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(UnassignPrivilegesFromRolesServiceRequest.class).verify();
  }
}
