package com.sap.olingo.jpa.processor.test.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataBatchProcessor;
import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestProcessor;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.processor.JPAODataRequestContextImpl;
import com.sap.olingo.jpa.processor.core.util.HttpServletRequestDouble;
import com.sap.olingo.jpa.processor.core.util.HttpServletResponseDouble;

public class IntegrationTestHelper {
  public final HttpServletRequestDouble req;
  public final HttpServletResponseDouble resp;
  private static final String uriPrefix = "http://localhost:8080/Test/Olingo.svc/";
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";
  private static final String[] enumPackages = { "com.sap.olingo.jpa.processor.core.testmodel" };
  private JPAODataCRUDContextAccess sessionContext;

  public IntegrationTestHelper(EntityManagerFactory emf, String urlPath, StringBuffer requestBody) throws IOException,
      ODataException {
    final OData odata = OData.newInstance();
    final EntityManager em = emf.createEntityManager();
    final Map<String, List<String>> headers = Collections.emptyMap();
    final JPAODataRequestContextImpl requestContext = new JPAODataRequestContextImpl();
    this.req = new HttpServletRequestDouble(uriPrefix + urlPath, requestBody, headers);
    this.resp = new HttpServletResponseDouble();
    this.sessionContext = mock(JPAODataCRUDContextAccess.class);

    final JPAEdmProvider edmProvider = new JPAEdmProvider(PUNIT_NAME, emf, null, enumPackages);
    when(sessionContext.getEdmProvider()).thenReturn(edmProvider);
    when(sessionContext.getDatabaseProcessor()).thenReturn(new JPADefaultDatabaseProcessor());
    when(sessionContext.getOperationConverter()).thenReturn(new JPADefaultDatabaseProcessor());
    final ODataHttpHandler handler = odata.createHandler(odata.createServiceMetadata(edmProvider,
        new ArrayList<EdmxReference>()));
    requestContext.setEntityManager(em);
    handler.register(new JPAODataRequestProcessor(sessionContext, requestContext));
    handler.register(new JPAODataBatchProcessor(sessionContext, requestContext));
    handler.process(req, resp);
  }

  public List<String> getRawBatchResult() throws IOException {
    List<String> result = new ArrayList<>();

    InputStream in = resp.getInputStream();
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    String read;

    while ((read = br.readLine()) != null) {
      result.add(read);
    }
    br.close();
    return result;
  }

  public int getBatchResultStatus(int index) throws IOException {
    List<String> result = getRawBatchResult();
    int count = 0;
    for (String resultLine : result) {
      if (resultLine.contains("HTTP/1.1")) {
        count += 1;
        if (count == index) {
          String[] statusElements = resultLine.split(" ");
          return Integer.parseInt(statusElements[1]);
        }
      }
    }
    return 0;
  }

  public JsonNode getBatchResult(int index) throws IOException {
    List<String> result = getRawBatchResult();
    int count = 0;
    boolean found = false;

    for (String resultLine : result) {
      if (resultLine.contains("HTTP/1.1")) {
        count += 1;
        if (count == index) {
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
