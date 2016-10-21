package org.apache.olingo.jpa.processor.core.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import org.apache.olingo.jpa.metadata.api.JPAEdmProvider;
import org.apache.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import org.apache.olingo.jpa.processor.core.api.JPAAbstractCUDRequestHandler;
import org.apache.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import org.apache.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import org.apache.olingo.jpa.processor.core.modify.JPACUDRequestHandler;
import org.apache.olingo.jpa.processor.core.modify.JPAConversionHelper;
import org.apache.olingo.jpa.processor.core.serializer.JPASerializer;
import org.apache.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import org.apache.olingo.jpa.processor.core.testmodel.Organization;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;

public class TestJPACreateProcessor {
  private static final String LOCATION_HEADER = "Organization('35')";
  private static final String PUNIT_NAME = "org.apache.olingo.jpa";
  private static EntityManagerFactory emf;
  private static JPAEdmProvider jpaEdm;
  private static DataSource ds;

  @BeforeClass
  public static void setupClass() throws ODataException {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
    jpaEdm = new JPAEdmProvider(PUNIT_NAME, emf.getMetamodel(), new JPAEdmMetadataPostProcessor() {
      @Override
      public void processNavigationProperty(IntermediateNavigationPropertyAccess property,
          String jpaManagedTypeClassName) {}

      @Override
      public void processProperty(IntermediatePropertyAccess property, String jpaManagedTypeClassName) {}
    });

  }

  private JPACUDRequestProcessor processor;
  private OData odata;
  private ServiceMetadata serviceMetadata;
  private JPAODataSessionContextAccess sessionContext;
  private JPAODataRequestContextAccess requestContext;
  private UriInfo uriInfo;
  private UriResourceEntitySet uriEts;
  private EntityManager em;
  private JPASerializer serializer;
  private EdmEntitySet ets;
  private List<UriParameter> keyPredicates;
  private JPAConversionHelper helper;
  private List<UriResource> pathParts = new ArrayList<UriResource>();
  private SerializerResult serializerResult;
  private List<String> header = new ArrayList<String>();

  @Before
  public void setUp() throws Exception {
    odata = OData.newInstance();
    sessionContext = mock(JPAODataSessionContextAccess.class);
    requestContext = mock(JPAODataRequestContextAccess.class);
    serviceMetadata = mock(ServiceMetadata.class);
    uriInfo = mock(UriInfo.class);
    keyPredicates = new ArrayList<UriParameter>();
    ets = mock(EdmEntitySet.class);
    serializer = mock(JPASerializer.class);
    uriEts = mock(UriResourceEntitySet.class);
    pathParts.add(uriEts);

    helper = mock(JPAConversionHelper.class);
    em = mock(EntityManager.class);
    serializerResult = mock(SerializerResult.class);

    when(sessionContext.getEdmProvider()).thenReturn(jpaEdm);
    when(requestContext.getEntityManager()).thenReturn(em);
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(requestContext.getSerializer()).thenReturn(serializer);
    when(uriInfo.getUriResourceParts()).thenReturn(pathParts);
    when(uriEts.getKeyPredicates()).thenReturn(keyPredicates);
    when(uriEts.getEntitySet()).thenReturn(ets);
    when(uriEts.getKind()).thenReturn(UriResourceKind.entitySet);
    when(ets.getName()).thenReturn("Organizations");
    processor = new JPACUDRequestProcessor(odata, serviceMetadata, sessionContext, requestContext, helper);
  }

  @Test
  public void testHockIsCalled() throws ODataJPAModelException, ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertTrue(spy.called);
  }

  @Test
  public void testEntityTypeProvided() throws ODataJPAProcessorException, SerializerException, ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals("Organization", spy.et.getExternalName());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testAttributesProvided() throws ODataJPAProcessorException, SerializerException, ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();
    Map<String, Object> attributes = new HashMap<String, Object>(1);

    attributes.put("ID", "35");

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    when(helper.convertProperties(Matchers.any(OData.class), Matchers.any(JPAStructuredType.class), Matchers.any(
        List.class))).thenReturn(attributes);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertNotNull(spy.jpaAttributes);
    assertEquals(1, spy.jpaAttributes.size());
    assertEquals("35", spy.jpaAttributes.get("ID"));
  }

  @Test
  public void testThrowExpectedExceptionInCaseOfError() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(sessionContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).createEntity(any(JPAEntityType.class), anyMapOf(String.class,
            Object.class), any(EntityManager.class));

    try {
      processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    } catch (ODataApplicationException e) {
      assertEquals(HttpStatusCode.BAD_REQUEST.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @Test
  public void testThrowUnexpectedExceptionInCaseOfError() throws ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(sessionContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(NullPointerException.class).when(handler).createEntity(any(JPAEntityType.class), anyMapOf(String.class,
        Object.class), any(EntityManager.class));

    try {
      processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    } catch (ODataApplicationException e) {
      assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @Test
  public void testMinimalResponseLocationHeader() throws ODataJPAProcessorException, SerializerException,
      ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(LOCATION_HEADER, response.getHeader(HttpHeader.LOCATION));
  }

  @Test
  public void testMinimalResponseStatusCode() throws ODataJPAProcessorException, SerializerException, ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testMinimalResponsePreferApplied() throws ODataJPAProcessorException, SerializerException,
      ODataException {
    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareSimpleRequest();

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals("return=minimal", response.getHeader(HttpHeader.PREFERENCE_APPLIED));
  }

  @Test
  public void testRepresentationResponseStatusCode() throws ODataJPAProcessorException, SerializerException,
      ODataException {

    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareRepresentationRequest();

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(HttpStatusCode.CREATED.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void testRepresentationResponseContent() throws ODataJPAProcessorException, SerializerException,
      ODataException, IOException {

    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareRepresentationRequest();

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    byte[] act = new byte[100];
    response.getContent().read(act);
    String s = new String(act).trim();
    assertEquals("{\"ID\":\"35\"}", s);
  }

  @Test
  public void testRepresentationLocationHeader() throws ODataJPAProcessorException, SerializerException,
      ODataException {

    ODataResponse response = new ODataResponse();
    ODataRequest request = prepareRepresentationRequest();

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(LOCATION_HEADER, response.getHeader(HttpHeader.LOCATION));
  }

  private ODataRequest prepareRepresentationRequest() throws ODataJPAProcessorException, SerializerException,
      ODataException {
    ODataRequest request = prepareSimpleRequest("return=representation");

    RequestHandleSpy spy = new RequestHandleSpy();
    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);
    Organization org = new Organization();
    when(em.find(Organization.class, "35")).thenReturn(org);
    org.setID("35");
    Edm edm = mock(Edm.class);
    when(serviceMetadata.getEdm()).thenReturn(edm);
    EdmEntityType edmET = mock(EdmEntityType.class);
    FullQualifiedName fqn = new FullQualifiedName("org.apache.olingo.jpa.Organization");
    when(edm.getEntityType(fqn)).thenReturn(edmET);
    List<String> keyNames = new ArrayList<String>();
    keyNames.add("ID");
    when(edmET.getKeyPredicateNames()).thenReturn(keyNames);
    EdmKeyPropertyRef refType = mock(EdmKeyPropertyRef.class);
    when(edmET.getKeyPropertyRef("ID")).thenReturn(refType);
    EdmProperty edmProperty = mock(EdmProperty.class);
    when(refType.getProperty()).thenReturn(edmProperty);
    when(refType.getName()).thenReturn("ID");
    EdmPrimitiveType type = mock(EdmPrimitiveType.class);
    when(edmProperty.getType()).thenReturn(type);
    when(type.toUriLiteral(Matchers.anyString())).thenReturn("35");

    when(serializer.serialize(Matchers.eq(request), Matchers.any(EntityCollection.class))).thenReturn(serializerResult);
    when(serializerResult.getContent()).thenReturn(new ByteArrayInputStream("{\"ID\":\"35\"}".getBytes()));

    return request;
  }

  private ODataRequest prepareSimpleRequest() throws ODataException,
      ODataJPAProcessorException, SerializerException {

    return prepareSimpleRequest("return=minimal");
  }

  private ODataRequest prepareSimpleRequest(String content) throws ODataException,
      ODataJPAProcessorException, SerializerException {

    EntityTransaction transaction = mock(EntityTransaction.class);
    when(em.getTransaction()).thenReturn(transaction);

    ODataRequest request = mock(ODataRequest.class);
    when(request.getHeaders(HttpHeader.PREFER)).thenReturn(header);
    when(sessionContext.getEdmProvider()).thenReturn(jpaEdm);

    header.add(content);

    Entity odataEntity = mock(Entity.class);
    when(helper.convertInputStream(odata, request, ContentType.JSON, ets)).thenReturn(odataEntity);
    when(helper.convertKeyToLocal(Matchers.eq(odata), Matchers.eq(request), Matchers.eq(ets), (JPAEntityType) Matchers
        .anyObject(), Matchers.anyObject())).thenReturn(LOCATION_HEADER);
    return request;
  }

  class RequestHandleSpy extends JPAAbstractCUDRequestHandler {
    public JPAEntityType et;
    public Map<String, Object> jpaAttributes;
    public EntityManager em;
    public boolean called = false;

    @Override
    public Object createEntity(JPAEntityType et, Map<String, Object> jpaAttributes, EntityManager em)
        throws ODataJPAProcessException {
      this.et = et;
      this.jpaAttributes = jpaAttributes;
      this.em = em;
      this.called = true;
      return "35";
    }
  }
}
