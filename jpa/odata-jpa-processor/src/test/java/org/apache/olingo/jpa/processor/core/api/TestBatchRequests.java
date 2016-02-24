package org.apache.olingo.jpa.processor.core.api;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.processor.core.util.IntegrationTestHelper;
import org.apache.olingo.jpa.processor.core.util.TestBase;
import org.junit.Test;

public class TestBatchRequests extends TestBase {

  @Test
  public void testOneGetRequest() throws IOException, ODataException {
    StringBuffer requestBody = new StringBuffer("--abc123");
    requestBody.append(' ');
    requestBody.append("requestBody.append(Content-Type: application/http)");
    requestBody.append(' ');
    requestBody.append(' ');

    requestBody.append(' ');

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "$batch");
    helper.assertStatus(200);
  }
}
