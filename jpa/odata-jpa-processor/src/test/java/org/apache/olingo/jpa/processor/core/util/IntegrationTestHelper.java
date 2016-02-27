package org.apache.olingo.jpa.processor.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.metadata.api.JPAEdmProvider;
import org.apache.olingo.jpa.processor.core.api.JPAODataBatchProcessor;
import org.apache.olingo.jpa.processor.core.api.JPAODataContextAccess;
import org.apache.olingo.jpa.processor.core.api.JPAODataRequestProcessor;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.edmx.EdmxReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class IntegrationTestHelper {
  public final HttpServletRequestDouble req;
  public final HttpServletResponseDouble resp;
  private static final String uriPrefix = "http://localhost:8080/Test/Olingo.svc/";
  private static final String PUNIT_NAME = "org.apache.olingo.jpa";

  public IntegrationTestHelper(EntityManagerFactory localEmf, String urlPath) throws IOException,
      ODataException {
    this(localEmf, null, urlPath, null);
  }

  public IntegrationTestHelper(EntityManagerFactory localEmf, DataSource ds, String urlPath) throws IOException,
      ODataException {
    this(localEmf, ds, urlPath, null);
  }

  public IntegrationTestHelper(EntityManagerFactory localEmf, String urlPath, StringBuffer requestBody)
      throws IOException, ODataException {
    this(localEmf, null, urlPath, requestBody);
  }

  public IntegrationTestHelper(EntityManagerFactory localEmf, DataSource ds, String urlPath, StringBuffer requestBody)
      throws IOException,
      ODataException {
    super();
    this.req = new HttpServletRequestDouble(uriPrefix + urlPath, requestBody);
    this.resp = new HttpServletResponseDouble();
    OData odata = OData.newInstance();
    JPAODataContextAccess context = new JPAODataContextAccessDouble(new JPAEdmProvider(PUNIT_NAME, localEmf, null), ds);

    ODataHttpHandler handler = odata.createHandler(odata.createServiceMetadata(context.getEdmProvider(),
        new ArrayList<EdmxReference>()));
    handler.register(new JPAODataRequestProcessor(context, localEmf.createEntityManager()));
    handler.register(new JPAODataBatchProcessor());
    handler.process(req, resp);

  }

  public HttpServletResponseDouble getResponce() {
    return resp;
  }

  public int getStatus() {
    return resp.getStatus();
  }

  public String getRawResult() throws IOException {
    InputStream in = resp.getInputStream();
    StringBuilder sb = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    String read;

    while ((read = br.readLine()) != null) {
      sb.append(read);
    }
    br.close();
    return sb.toString();
  }

  public List<String> getRawBatchResult() throws IOException {
    List<String> result = new ArrayList<String>();

    InputStream in = resp.getInputStream();
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    String read;

    while ((read = br.readLine()) != null) {
      result.add(read);
    }
    br.close();
    return result;
  }

  public ArrayNode getValues() throws JsonProcessingException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(getRawResult());
    if (!(node.get("value") instanceof ArrayNode))
      fail("Wrong result type; ArrayNode expected");
    ArrayNode values = (ArrayNode) node.get("value");
    return values;
  }

  public ObjectNode getValue() throws JsonProcessingException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode value = mapper.readTree(getRawResult());
    if (!(value instanceof ObjectNode))
      fail("Wrong result type; ObjectNode expected");
    return (ObjectNode) value;
  }

  public void assertStatus(int exp) throws IOException {
    assertEquals(getRawResult(), exp, getStatus());

  }

  public int getBatchResultStatus(int i) throws IOException {
    List<String> result = getRawBatchResult();
    int count = 0;
    for (String resultLine : result) {
      if (resultLine.contains("HTTP/1.1")) {
        count += 1;
        if (count == i) {
          String[] statusElements = resultLine.split(" ");
          return Integer.parseInt(statusElements[1]);
        }
      }
    }
    return 0;
  }

  public JsonNode getBatchResult(int i) throws IOException {
    List<String> result = getRawBatchResult();
    int count = 0;
    boolean found = false;

    for (String resultLine : result) {
      if (resultLine.contains("HTTP/1.1")) {
        count += 1;
        if (count == i) {
          found = true;
        }
      }
      if (found && resultLine.startsWith("{")) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(resultLine);
      }
    }
    return null;
  }
}
