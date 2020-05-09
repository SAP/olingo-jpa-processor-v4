package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.test.util.IntegrationTestHelper;

public class JPAODataParallelBatchProcessorIntegrationTest {

  private static final String PUNIT_NAME = "com.sap.olingo.jpa";

  private static EntityManagerFactory emf;
  private static DataSource ds;

  @BeforeAll
  public static void setupClass() throws ODataJPAModelException {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_H2);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
  }

  @Test
  public void testOneGetRequestGetResponce() throws IOException, ODataException {
    final StringBuilder requestBody = createBodyOneGet();

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "$batch", requestBody);
    final List<String> act = helper.getRawBatchResult();
    assertNotNull(act);
  }

  @Test
  public void testOneGetRequestCheckStatus() throws IOException, ODataException {
    final StringBuilder requestBody = createBodyOneGet();

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "$batch", requestBody);
    assertEquals(200, helper.getBatchResultStatus(1));
  }

  @Test
  public void testOneGetRequestCheckValue() throws IOException, ODataException {
    final StringBuilder requestBody = createBodyOneGet();

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "$batch", requestBody);
    assertEquals(200, helper.getBatchResultStatus(1));
    final JsonNode value = helper.getBatchResult(1);
    assertEquals("3", value.get("ID").asText());
  }

  @Test
  public void testTwoGetRequestSecondFailCheckStatus() throws IOException, ODataException {
    final StringBuilder requestBody = createBodyTwoGetOneFail();

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "$batch", requestBody);
    assertEquals(404, helper.getBatchResultStatus(2));
  }

  @Test
  public void testTwoGetRequestCheckValue() throws IOException, ODataException {
    final StringBuilder requestBody = createBodyTwoGet();

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "$batch", requestBody);

    JsonNode value = helper.getBatchResult(1);
    System.out.println(value);
    assertEquals(200, helper.getBatchResultStatus(1));
    assertNotNull(value.get("ID"));
    assertEquals("3", value.get("ID").asText());

    value = helper.getBatchResult(2);
    System.out.println(value);
    assertEquals(200, helper.getBatchResultStatus(2));
    assertNotNull(value.get("ID"));
    assertEquals("5", value.get("ID").asText());
  }

  private StringBuilder createBodyTwoGetOneFail() {
    final StringBuilder requestBody = new StringBuilder("--abc123\r\n");
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

  private StringBuilder createBodyTwoGet() {
    final StringBuilder requestBody = new StringBuilder("--abc123\r\n");
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

  private StringBuilder createBodyOneGet() {
    final StringBuilder requestBody = new StringBuilder("--abc123\r\n");
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
