package com.sitepark.ies.application.role;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class CreateRoleServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(CreateRoleServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(CreateRoleServiceRequest.class).verify();
  }
}
