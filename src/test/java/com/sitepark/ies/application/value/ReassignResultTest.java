package com.sitepark.ies.application.value;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ReassignResultTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(ReassignResult.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(ReassignResult.class).verify();
  }

  @Test
  void testEmpty() {
    assertEquals(
        new ReassignResult(0, 0),
        ReassignResult.empty(),
        "empty() should return a ReassignResult with assigned=0 and unassigned=0");
  }
}
