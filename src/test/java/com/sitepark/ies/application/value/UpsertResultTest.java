package com.sitepark.ies.application.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UpsertResultTest {

  @Test
  void testCreatedEquals() {
    EqualsVerifier.forClass(UpsertResult.Created.class).verify();
  }

  @Test
  void testCreatedToString() {
    ToStringVerifier.forClass(UpsertResult.Created.class).verify();
  }

  @Test
  void testUpdatedEquals() {
    EqualsVerifier.forClass(UpsertResult.Updated.class).verify();
  }

  @Test
  void testUpdatedToString() {
    ToStringVerifier.forClass(UpsertResult.Updated.class).verify();
  }

  @Test
  void testCreatedFactoryReturnsCreatedInstance() {
    UpsertResult result = UpsertResult.created("123");

    assertInstanceOf(
        UpsertResult.Created.class, result, "created() factory should return a Created instance");
  }

  @Test
  void testCreatedFactoryReturnsIdFromParameter() {
    UpsertResult.Created result = UpsertResult.created("123");

    assertEquals("123", result.id(), "created() factory should preserve the provided id");
  }

  @Test
  void testUpdatedFactoryReturnsUpdatedInstance() {
    UpsertResult result = UpsertResult.updated(true);

    assertInstanceOf(
        UpsertResult.Updated.class, result, "updated() factory should return an Updated instance");
  }

  @Test
  void testUpdatedFactoryPreservesFlag() {
    UpsertResult.Updated result = UpsertResult.updated(false);

    assertEquals(false, result.updated(), "updated() factory should preserve the updated flag");
  }
}
