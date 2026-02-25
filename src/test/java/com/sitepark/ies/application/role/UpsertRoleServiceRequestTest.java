package com.sitepark.ies.application.role;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UpsertRoleServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(UpsertRoleServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(UpsertRoleServiceRequest.class).verify();
  }
}
