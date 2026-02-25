package com.sitepark.ies.application.privilege;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UpsertPrivilegeServiceRequestTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(UpsertPrivilegeServiceRequest.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(UpsertPrivilegeServiceRequest.class).verify();
  }
}
