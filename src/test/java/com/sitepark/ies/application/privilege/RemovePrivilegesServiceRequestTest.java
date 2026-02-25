package com.sitepark.ies.application.privilege;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class RemovePrivilegesServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(RemovePrivilegesServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(RemovePrivilegesServiceRequest.class).verify();
  }
}
