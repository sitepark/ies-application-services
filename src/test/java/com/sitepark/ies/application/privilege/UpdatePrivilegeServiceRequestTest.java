package com.sitepark.ies.application.privilege;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UpdatePrivilegeServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(UpdatePrivilegeServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(UpdatePrivilegeServiceRequest.class).verify();
  }
}
