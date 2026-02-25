package com.sitepark.ies.application.audit.revert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.sitepark.ies.audit.core.service.RevertRequest;
import org.junit.jupiter.api.Test;

class RevertFailedExceptionTest {

  @Test
  void testGetRequestReturnsRequest() {
    RevertRequest request = mock();
    RevertFailedException exception = new RevertFailedException(request, "test message");
    assertEquals(
        request,
        exception.getRequest(),
        "getRequest() should return the request passed to constructor");
  }
}
