package com.sitepark.ies.application.role;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UpdateRoleServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(UpdateRoleServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(UpdateRoleServiceRequest.class).verify();
  }
}
