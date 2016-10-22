package org.apache.olingo.jpa.processor.core.processor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import org.apache.olingo.jpa.metadata.api.JPAEdmProvider;
import org.apache.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import org.apache.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import org.apache.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import org.apache.olingo.jpa.processor.core.modify.JPAConversionHelper;
import org.apache.olingo.jpa.processor.core.processor.TestJPACreateProcessor.RequestHandleSpy;
import org.apache.olingo.jpa.processor.core.serializer.JPASerializer;
import org.apache.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import org.apache.olingo.jpa.processor.core.testmodel.Organization;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
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
import org.mockito.Matchers;

public abstract class TestJPAModifyProcessor {
  protected static final String LOCATION_HEADER = "Organization('35')";
  protected static final String PUNIT_NAME = "org.apache.olingo.jpa";
  protected static EntityManagerFactory emf;
  protected static JPAEdmProvider jpaEdm;
  protected static DataSource ds;

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

  protected JPACUDRequestProcessor processor;
  protected OData odata;
  protected ServiceMetadata serviceMetadata;
  protected JPAODataSessionContextAccess sessionContext;
  protected JPAODataRequestContextAccess requestContext;
  protected UriInfo uriInfo;
  protected UriResourceEntitySet uriEts;
  protected EntityManager em;
  protected JPASerializer serializer;
  protected EdmEntitySet ets;
  protected List<UriParameter> keyPredicates;
  protected JPAConversionHelper helper;
  protected List<UriResource> pathParts = new ArrayList<UriResource>();
  protected SerializerResult serializerResult;
  protected List<String> header = new ArrayList<String>();

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

  protected ODataRequest prepareRepresentationRequest(RequestHandleSpy spy) throws ODataJPAProcessorException,
      SerializerException,
      ODataException {
    ODataRequest request = prepareSimpleRequest("return=representation");

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

  protected ODataRequest prepareSimpleRequest() throws ODataException, ODataJPAProcessorException, SerializerException {

    return prepareSimpleRequest("return=minimal");
  }

  private ODataRequest prepareSimpleRequest(String content) throws ODataException, ODataJPAProcessorException,
      SerializerException {

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
}
