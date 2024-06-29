package com.sap.olingo.jpa.processor.test.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.mockito.Answers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataBatchProcessor;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestProcessor;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.processor.JPAODataInternalRequestContext;
import com.sap.olingo.jpa.processor.core.util.HttpRequestHeaderDouble;
import com.sap.olingo.jpa.processor.core.util.ServletInputStreamDouble;

public class IntegrationTestHelper {
  public final HttpServletRequest req;
  public final HttpServletResponse resp;
  private static final String uriPrefix = "http://localhost:8080/Test/Olingo.svc/";
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";
  private static final String[] enumPackages = { "com.sap.olingo.jpa.processor.core.testmodel" };
  private final JPAODataSessionContextAccess sessionContext;
  private final JPAODataRequestContext customContext;

  public IntegrationTestHelper(final EntityManagerFactory emf, final String urlPath, final StringBuilder requestBody)
      throws IOException, ODataException {

    final OData odata = OData.newInstance();
    final EntityManager em = emf.createEntityManager();
    final Map<String, List<String>> headers = Collections.emptyMap();
    this.req = getRequestMock(uriPrefix + urlPath, requestBody, headers);
    this.resp = getResponseMock();
    this.customContext = mock(JPAODataRequestContext.class);
    this.sessionContext = mock(JPAODataSessionContextAccess.class);
    final JPAEdmProvider edmProvider = new JPAEdmProvider(PUNIT_NAME, emf, null, enumPackages);
    when(sessionContext.getEdmProvider()).thenReturn(edmProvider);
    when(sessionContext.getDatabaseProcessor()).thenReturn(new JPADefaultDatabaseProcessor());
    when(sessionContext.getOperationConverter()).thenReturn(new JPADefaultDatabaseProcessor());
    when(customContext.getEntityManager()).thenReturn(em);
    final ODataHttpHandler handler = odata.createHandler(odata.createServiceMetadata(edmProvider,
        new ArrayList<>()));
    final JPAODataInternalRequestContext requestContext = new JPAODataInternalRequestContext(customContext,
        sessionContext, odata);
    handler.register(new JPAODataRequestProcessor(sessionContext, requestContext));
    handler.register(new JPAODataBatchProcessor(sessionContext, requestContext));
    handler.process(req, resp);
  }

  public List<String> getRawBatchResult() throws IOException {
    final List<String> result = new ArrayList<>();

    final InputStream in = asInputStream();
    final BufferedReader br = new BufferedReader(new InputStreamReader(in));
    String read;

    while ((read = br.readLine()) != null) {
      result.add(read);
    }
    br.close();
    return result;
  }

  public int getBatchResultStatus(final int index) throws IOException {
    final List<String> result = getRawBatchResult();
    int count = 0;
    for (final String resultLine : result) {
      if (resultLine.contains("HTTP/1.1")) {
        count += 1;
        if (count == index) {
          final String[] statusElements = resultLine.split(" ");
          return Integer.parseInt(statusElements[1]);
        }
      }
    }
    return 0;
  }

  public InputStream asInputStream() throws IOException {
    return new ResultStream((OutPutStream) resp.getOutputStream());
  }

  public JsonNode getBatchResult(final int index) throws IOException {
    final List<String> result = getRawBatchResult();
    int count = 0;
    boolean found = false;

    for (final String resultLine : result) {
      if (resultLine.contains("HTTP/1.1")) {
        count += 1;
        if (count == index) {
          found = true;
        }
      }
      if (found && resultLine.startsWith("{")) {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(resultLine);
      }
    }
    return null;
  }

  public HttpServletRequest getRequestMock(final String uri, final StringBuilder body,
      final Map<String, List<String>> headers) throws IOException {

    final HttpRequestHeaderDouble reqHeader = new HttpRequestHeaderDouble();
    final HttpServletRequest response = mock(HttpServletRequest.class);
    final String[] uriParts = uri.split("\\?");

    reqHeader.setHeaders(headers);
    if (uri.contains("$batch")) {
      when(response.getMethod()).thenReturn(HttpMethod.POST.toString());
      reqHeader.setBatchRequest();
    } else {
      when(response.getMethod()).thenReturn(HttpMethod.GET.toString());
    }
    when(response.getInputStream()).thenReturn(new ServletInputStreamDouble(body));
    when(response.getProtocol()).thenReturn("HTTP/1.1");
    when(response.getServletPath()).thenReturn("/Olingo.svc");
    when(response.getQueryString()).thenReturn((uriParts.length == 2) ? uriParts[1] : null);
    when(response.getRequestURL()).thenReturn(new StringBuffer(uriParts[0]));
    when(response.getHeaderNames()).thenReturn(reqHeader.getEnumerator());
    when(response.getCharacterEncoding()).thenReturn("UTF8");
    final Enumeration<String> headerEnumerator = reqHeader.getEnumerator();
    while (headerEnumerator.hasMoreElements()) {
      final String header = headerEnumerator.nextElement();
      when(response.getHeaders(header)).thenReturn(reqHeader.get(header));
    }
    return response;
  }

  public HttpServletResponse getResponseMock() throws IOException {
    final HttpServletResponse response = mock(HttpServletResponse.class, Answers.RETURNS_MOCKS);
    when(response.getOutputStream()).thenReturn(new OutPutStream());
    return response;

  }

  class OutPutStream extends ServletOutputStream {
    List<Integer> buffer = new ArrayList<>();

    @Override
    public void write(final int b) throws IOException {
      buffer.add(b);
    }

    public Iterator<Integer> getBuffer() {
      return buffer.iterator();
    }

    public int getSize() {
      return buffer.size();
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setWriteListener(final WriteListener writeListener) {

    }
  }

  //
  class ResultStream extends InputStream {
    private final Iterator<Integer> bufferExcess;

    public ResultStream(final OutPutStream buffer) {
      super();
      this.bufferExcess = buffer.getBuffer();
    }

    @Override
    public int read() throws IOException {
      if (bufferExcess.hasNext())
        return bufferExcess.next();
      return -1;
    }
  }
}
