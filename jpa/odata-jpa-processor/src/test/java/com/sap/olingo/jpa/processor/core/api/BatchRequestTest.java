package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class BatchRequestTest extends TestBase {

  @Test
  void testOneGetRequestGetResponse() throws IOException, ODataException {
    final StringBuffer requestBody = createBodyOneGet();

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "$batch", requestBody);
    final List<String> act = helper.getRawBatchResult();
    assertNotNull(act);
  }

  @Test
  void testOneGetRequestCheckStatus() throws IOException, ODataException {
    final StringBuffer requestBody = createBodyOneGet();

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "$batch", requestBody);
    assertEquals(200, helper.getBatchResultStatus(1));
  }

  @Test
  void testOneGetRequestCheckValue() throws IOException, ODataException {
    final StringBuffer requestBody = createBodyOneGet();

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "$batch", requestBody);
    final JsonNode value = helper.getBatchResult(1);
    assertEquals("3", value.get("ID").asText());
  }

  @Test
  void testTwoGetRequestSecondFailCheckStatus() throws IOException, ODataException {
    final StringBuffer requestBody = createBodyTwoGetOneFail();

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "$batch", requestBody);
    assertEquals(404, helper.getBatchResultStatus(2));
  }

  @Test
  void testTwoGetRequestCheckValue() throws IOException, ODataException {
    final StringBuffer requestBody = createBodyTwoGet();

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "$batch", requestBody);

    JsonNode value = helper.getBatchResult(1);
    assertEquals(200, helper.getBatchResultStatus(1));
    assertNotNull(value.get("ID"));
    assertEquals("3", value.get("ID").asText());

    value = helper.getBatchResult(2);
    assertEquals(200, helper.getBatchResultStatus(2));
    assertNotNull(value.get("ID"));
    assertEquals("5", value.get("ID").asText());
  }

  private StringBuffer createBodyTwoGetOneFail() {
    final StringBuffer requestBody = new StringBuffer("--abc123\r\n");
    requestBody.append("Content-Type: application/http\r\n");
    requestBody.append("Content-Transfer-Encoding: binary\r\n");
    requestBody.append("\r\n");
    requestBody.append("GET Organizations('3') HTTP/1.1\r\n");
    requestBody.append("Content-Type: application/json\r\n");
    requestBody.append("\r\n");
    requestBody.append("\r\n");
    requestBody.append("--abc123\r\n");
    requestBody.append("Content-Type: application/http\r\n");
    requestBody.append("Content-Transfer-Encoding: binary\r\n");
    requestBody.append("\r\n");
    requestBody.append("GET AdministrativeDivision HTTP/1.1\r\n");
    requestBody.append("Content-Type: application/json\r\n");
    requestBody.append("\r\n");
    requestBody.append("\r\n");
    requestBody.append("--abc123--");
    return requestBody;
  }

  private StringBuffer createBodyTwoGet() {
    final StringBuffer requestBody = new StringBuffer("--abc123\r\n");
    requestBody.append("Content-Type: application/http\r\n");
    requestBody.append("Content-Transfer-Encoding: binary\r\n");
    requestBody.append("\r\n");
    requestBody.append("GET Organizations('3') HTTP/1.1\r\n");
    requestBody.append("Content-Type: application/json\r\n");
    requestBody.append("\r\n");
    requestBody.append("\r\n");
    requestBody.append("--abc123\r\n");
    requestBody.append("Content-Type: application/http\r\n");
    requestBody.append("Content-Transfer-Encoding: binary\r\n");
    requestBody.append("\r\n");
    requestBody.append("GET Organizations('5') HTTP/1.1\r\n");
    requestBody.append("Content-Type: application/json\r\n");
    requestBody.append("\r\n");
    requestBody.append("\r\n");
    requestBody.append("--abc123--");
    return requestBody;
  }

  private StringBuffer createBodyOneGet() {
    final StringBuffer requestBody = new StringBuffer("--abc123\r\n");
    requestBody.append("Content-Type: application/http\r\n");
    requestBody.append("Content-Transfer-Encoding: binary\r\n");
    requestBody.append("\r\n");
    requestBody.append("GET Organizations('3') HTTP/1.1\r\n");
    requestBody.append("Content-Type: application/json\r\n");
    requestBody.append("\r\n");
    requestBody.append("\r\n");
    requestBody.append("--abc123--");
    return requestBody;
  }
}
