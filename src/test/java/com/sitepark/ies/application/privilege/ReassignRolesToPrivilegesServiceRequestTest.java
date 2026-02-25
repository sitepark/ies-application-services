package com.sitepark.ies.application.privilege;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ReassignRolesToPrivilegesServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(ReassignRolesToPrivilegesServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(ReassignRolesToPrivilegesServiceRequest.class).verify();
  }
}
