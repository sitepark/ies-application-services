package com.sitepark.ies.application.privilege;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class CreatePrivilegeServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(CreatePrivilegeServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(CreatePrivilegeServiceRequest.class).verify();
  }
}
