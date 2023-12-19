package com.sap.olingo.jpa.processor.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.debug.DefaultDebugSupport;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataBatchProcessor;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataContextAccessDouble;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataPagingProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestProcessor;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.processor.JPAODataInternalRequestContext;

public class IntegrationTestHelper {
  public final HttpServletRequest request;
  public final HttpServletResponse response;
  private final ArgumentCaptor<Integer> captorStatus;
  private static final String uriPrefix = "http://localhost:8080/Test/Olingo.svc/";
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";

  public IntegrationTestHelper(final EntityManagerFactory localEmf, final String urlPath) throws IOException,
      ODataException {
    this(localEmf, null, urlPath, null, null, null);
  }

  public IntegrationTestHelper(final EntityManagerFactory localEmf, final String urlPath,
      final AnnotationProvider annotationsProvider) throws IOException, ODataException {
    this(localEmf, null, urlPath, null, null, null, null, null, null, annotationsProvider);
  }

  public IntegrationTestHelper(final EntityManagerFactory localEmf, final String urlPath,
      final Map<String, List<String>> headers) throws IOException, ODataException {
    this(localEmf, null, urlPath, null, null, null, headers, null, null, null);
  }

  public IntegrationTestHelper(final EntityManagerFactory localEmf, final String urlPath,
      final JPAODataGroupProvider groups) throws IOException, ODataException {
    this(localEmf, null, urlPath, null, null, null, null, null, groups, null);
  }

  public IntegrationTestHelper(final EntityManagerFactory localEmf, final DataSource dataSource, final String urlPath)
      throws IOException, ODataException {
    this(localEmf, dataSource, urlPath, null, null, null);
  }

  public IntegrationTestHelper(final EntityManagerFactory localEmf, final String urlPath,
      final StringBuffer requestBody)
      throws IOException, ODataException {
    this(localEmf, null, urlPath, requestBody, null, null);
  }

  public IntegrationTestHelper(final EntityManagerFactory localEmf, final DataSource dataSource, final String urlPath,
      final String functionPackage)
      throws IOException, ODataException {
    this(localEmf, dataSource, urlPath, null, functionPackage, null);
  }

  public IntegrationTestHelper(final EntityManagerFactory localEmf, final DataSource dataSource, final String urlPath,
      final StringBuffer requestBody)
      throws IOException, ODataException {
    this(localEmf, dataSource, urlPath, requestBody, null, null);
  }

  public IntegrationTestHelper(final EntityManagerFactory localEmf, final String urlPath,
      final JPAODataPagingProvider provider) throws IOException, ODataException {
    this(localEmf, null, urlPath, null, null, provider);
  }

  public IntegrationTestHelper(final EntityManagerFactory localEmf, final String urlPath,
      final JPAODataClaimsProvider claims)
      throws IOException, ODataException {
    this(localEmf, null, urlPath, null, null, null, null, claims, null, null);
  }

  public IntegrationTestHelper(final EntityManagerFactory localEmf, final String urlPath,
      final JPAODataPagingProvider provider, final JPAODataClaimsProvider claims) throws IOException, ODataException {
    this(localEmf, null, urlPath, null, null, provider, null, claims, null, null);
  }

  public IntegrationTestHelper(final EntityManagerFactory emf, final String urlPath,
      final JPAODataPagingProvider provider, final Map<String, List<String>> headers) throws IOException,
      ODataException {
    this(emf, null, urlPath, null, null, provider, headers, null, null, null);
  }

  public IntegrationTestHelper(final EntityManagerFactory localEmf, final DataSource dataSource, final String urlPath,
      final StringBuffer requestBody,
      final String functionPackage, final JPAODataPagingProvider provider) throws IOException, ODataException {
    this(localEmf, dataSource, urlPath, requestBody, functionPackage, provider, null, null, null, null);
  }

  public IntegrationTestHelper(final EntityManagerFactory localEmf, final DataSource dataSource, final String urlPath,
      final StringBuffer requestBody, final String functionPackage, final JPAODataPagingProvider pagingProvider,
      final Map<String, List<String>> headers, final JPAODataClaimsProvider claims, final JPAODataGroupProvider groups,
      final AnnotationProvider annotationsProvider)
      throws IOException, ODataException {

    super();
    final OData odata = OData.newInstance();
    String[] packages = TestBase.enumPackages;
    captorStatus = ArgumentCaptor.forClass(Integer.class);
    this.request = getRequestMock(uriPrefix + urlPath,
        requestBody == null ? null : new StringBuilder(requestBody.toString()), headers);
    this.response = getResponseMock();
    if (functionPackage != null)
      packages = ArrayUtils.add(packages, functionPackage);
    final JPAEdmProvider edmProvider = new JPAEdmProvider(PUNIT_NAME, localEmf, null, packages,
        annotationsProvider == null ? Collections.emptyList() : Collections.singletonList(annotationsProvider));

    final EntityManager em = createEmfWrapper(localEmf, edmProvider).createEntityManager();

    final JPAODataSessionContextAccess sessionContext = new JPAODataContextAccessDouble(edmProvider, dataSource,
        pagingProvider, annotationsProvider, functionPackage);

    final ODataHttpHandler handler = odata.createHandler(odata.createServiceMetadata(sessionContext.getEdmProvider(),
        new ArrayList<>()));

    final JPAODataRequestContext externalContext = mock(JPAODataRequestContext.class);
    when(externalContext.getEntityManager()).thenReturn(em);
    when(externalContext.getClaimsProvider()).thenReturn(Optional.ofNullable(claims));
    when(externalContext.getGroupsProvider()).thenReturn(Optional.ofNullable(groups));
    when(externalContext.getDebuggerSupport()).thenReturn(new DefaultDebugSupport());
    when(externalContext.getRequestParameter()).thenReturn(mock(JPARequestParameterMap.class));
    final JPAODataInternalRequestContext requestContext = new JPAODataInternalRequestContext(externalContext,
        sessionContext);

    handler.register(new JPAODataRequestProcessor(sessionContext, requestContext));
    handler.register(new JPAODataBatchProcessor(sessionContext, requestContext));
    handler.process(request, response);

  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public int getStatus() {
    verify(response).setStatus(captorStatus.capture());
    return captorStatus.getValue();
  }

  public String getRawResult() throws IOException {
    final InputStream in = asInputStream();
    final StringBuilder builder = new StringBuilder();
    final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String read;

    while ((read = reader.readLine()) != null) {
      builder.append(read);
    }
    reader.close();
    return builder.toString();
  }

  public List<String> getRawBatchResult() throws IOException {
    final List<String> result = new ArrayList<>();

    final InputStream in = asInputStream();
    final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String read;

    while ((read = reader.readLine()) != null) {
      result.add(read);
    }
    reader.close();
    return result;
  }

  public InputStream asInputStream() throws IOException {
    return new ResultStream((OutPutStream) response.getOutputStream());
  }

  public ArrayNode getValues() throws JsonProcessingException, IOException {
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode node = mapper.readTree(getRawResult());
    if (!(node.get("value") instanceof ArrayNode))
      fail("Wrong result type; ArrayNode expected");
    final ArrayNode values = (ArrayNode) node.get("value");
    return values;
  }

  public ObjectNode getValue() throws JsonProcessingException, IOException {
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode value = mapper.readTree(getRawResult());
    if (!(value instanceof ObjectNode))
      fail("Wrong result type; ObjectNode expected");
    return (ObjectNode) value;
  }

  public ValueNode getSingleValue() throws JsonProcessingException, IOException {
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode value = mapper.readTree(getRawResult());
    if (!(value instanceof ValueNode))
      fail("Wrong result type; ValueNode expected");
    return (ValueNode) value;
  }

  public ValueNode getSingleValue(final String nodeName) throws JsonProcessingException, IOException {
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode node = mapper.readTree(getRawResult());
    if (!(node.get(nodeName) instanceof ValueNode))
      fail("Wrong result type; ArrayNode expected");
    return (ValueNode) node.get(nodeName);
  }

  public void assertStatus(final int exp) throws IOException {
    assertEquals(exp, getStatus(), getRawResult());

  }

  public int getBatchResultStatus(final int i) throws IOException {
    final List<String> result = getRawBatchResult();
    int count = 0;
    for (final String resultLine : result) {
      if (resultLine.contains("HTTP/1.1")) {
        count += 1;
        if (count == i) {
          final String[] statusElements = resultLine.split(" ");
          return Integer.parseInt(statusElements[1]);
        }
      }
    }
    return 0;
  }

  public JsonNode getBatchResult(final int i) throws IOException {
    final List<String> result = getRawBatchResult();
    int count = 0;
    boolean found = false;

    for (final String resultLine : result) {
      if (resultLine.contains("HTTP/1.1")) {
        count += 1;
        if (count == i) {
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

  public byte[] getBinaryResult() throws IOException {

    final InputStream in = asInputStream();
    final byte[] result = new byte[((ResultStream) in).getSize()];
    in.read(result);
    return result;
  }

  public static HttpServletRequest getRequestMock(final String uri) throws IOException {
    return getRequestMock(uri, null);
  }

  public static HttpServletRequest getRequestMock(final String uri, final StringBuilder body) throws IOException {
    return getRequestMock(uri, body, Collections.emptyMap());
  }

  public static HttpServletRequest getRequestMock(final String uri, final StringBuilder body,
      final Map<String, List<String>> headers) throws IOException {

    final HttpRequestHeaderDouble requestHeader = new HttpRequestHeaderDouble();
    final HttpServletRequest response = mock(HttpServletRequest.class);
    final String[] uriParts = uri.split("\\?");

    requestHeader.setHeaders(headers);
    if (uri.contains("$batch")) {
      when(response.getMethod()).thenReturn(HttpMethod.POST.toString());
      requestHeader.setBatchRequest();
    } else {
      when(response.getMethod()).thenReturn(HttpMethod.GET.toString());
    }
    when(response.getInputStream()).thenReturn(new ServletInputStreamDouble(body));
    when(response.getProtocol()).thenReturn("HTTP/1.1");
    when(response.getServletPath()).thenReturn("/Olingo.svc");
    when(response.getQueryString()).thenReturn((uriParts.length == 2) ? uriParts[1] : null);
    when(response.getRequestURL()).thenReturn(new StringBuffer(uriParts[0]));
    when(response.getHeaderNames()).thenReturn(requestHeader.getEnumerator());
    final Enumeration<String> headerEnumerator = requestHeader.getEnumerator();
    while (headerEnumerator.hasMoreElements()) {
      final String header = headerEnumerator.nextElement();
      when(response.getHeaders(header)).thenReturn(requestHeader.get(header));
    }
    return response;
  }

  public static HttpServletResponse getResponseMock() throws IOException {
    final HttpServletResponse response = mock(HttpServletResponse.class, Answers.RETURNS_MOCKS);
    when(response.getOutputStream()).thenReturn(new OutPutStream());
    return response;

  }

  @SuppressWarnings("unchecked")
  private EntityManagerFactory createEmfWrapper(@Nonnull final EntityManagerFactory emf,
      @Nonnull final JPAEdmProvider jpaEdm) throws ODataException {

    try {
      final Class<? extends EntityManagerFactory> wrapperClass = (Class<? extends EntityManagerFactory>) Class
          .forName("com.sap.olingo.jpa.processor.cb.api.EntityManagerFactoryWrapper");

      try {
        return wrapperClass.getConstructor(EntityManagerFactory.class,
            JPAServiceDocument.class).newInstance(emf, jpaEdm.getServiceDocument());
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
          | NoSuchMethodException | SecurityException e) {
        throw new ODataException(e.getMessage());
      }
    } catch (final ClassNotFoundException e) {
      return emf;
    }
  }

  private static class OutPutStream extends ServletOutputStream {
    List<Integer> buffer = new ArrayList<>();

    @Override
    public void write(final int nextByte) throws IOException {
      buffer.add(nextByte);
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
    private final int size;

    public ResultStream(final OutPutStream buffer) {
      super();
      this.bufferExcess = buffer.getBuffer();
      this.size = buffer.getSize();
    }

    @Override
    public int read() throws IOException {
      if (bufferExcess.hasNext())
        return bufferExcess.next();
      return -1;
    }

    public int getSize() {
      return size;
    }
  }

}
