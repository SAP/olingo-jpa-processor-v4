package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;
import com.sap.olingo.jpa.processor.core.api.JPAAbstractCUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPACUDRequestHandler;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

public class TestJPADeleteProcessor {
  private JPACUDRequestProcessor       processor;
  private OData                        odata;
  private ServiceMetadata              serviceMetadata;
  private JPAODataSessionContextAccess sessionContext;
  private JPAODataRequestContextAccess requestContext;
  private UriInfo                      uriInfo;
  private UriResourceEntitySet         uriEts;
  private EdmEntitySet                 ets;
  private List<UriParameter>           keyPredicates;

  private static final String         PUNIT_NAME = "org.apache.olingo.jpa";
  private static EntityManagerFactory emf;
  private static JPAEdmProvider       jpaEdm;
  private static DataSource           ds;

  @BeforeClass
  public static void setupClass() throws ODataException {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
    jpaEdm = new JPAEdmProvider(PUNIT_NAME, emf.getMetamodel(), new JPAEdmMetadataPostProcessor() {
      @Override
      public void processProperty(IntermediatePropertyAccess property, String jpaManagedTypeClassName) {}

      @Override
      public void processNavigationProperty(IntermediateNavigationPropertyAccess property,
          String jpaManagedTypeClassName) {}

      @Override
      public void provideReferences(IntermediateReferenceList references) throws ODataJPAModelException {}
    });

  }

  @Before
  public void setUp() throws Exception {
    odata = OData.newInstance();
    sessionContext = mock(JPAODataSessionContextAccess.class);
    requestContext = mock(JPAODataRequestContextAccess.class);
    serviceMetadata = mock(ServiceMetadata.class);
    uriInfo = mock(UriInfo.class);
    keyPredicates = new ArrayList<UriParameter>();
    ets = mock(EdmEntitySet.class);
    final List<UriResource> pathParts = new ArrayList<UriResource>();
    uriEts = mock(UriResourceEntitySet.class);
    pathParts.add(uriEts);

    when(sessionContext.getEdmProvider()).thenReturn(jpaEdm);
    when(requestContext.getEntityManager()).thenReturn(emf.createEntityManager());
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getUriResourceParts()).thenReturn(pathParts);
    when(uriEts.getKeyPredicates()).thenReturn(keyPredicates);
    when(uriEts.getEntitySet()).thenReturn(ets);
    when(ets.getName()).thenReturn("Organizations");
    processor = new JPACUDRequestProcessor(odata, serviceMetadata, sessionContext, requestContext,
        new JPAConversionHelper());
  }

  @Test
  public void testSuccessReturnCode() throws ODataApplicationException {
    ODataResponse response = new ODataResponse();
    when(sessionContext.getCUDRequestHandler()).thenReturn(new RequestHandleSpy());

    processor.deleteEntity(response);
    assertEquals(204, response.getStatusCode());
  }

  @Test
  public void testThrowUnexpectedExceptionInCaseOfError() throws ODataJPAProcessException {
    ODataResponse response = new ODataResponse();
    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    doThrow(NullPointerException.class).when(handler).deleteEntity(any(JPAEntityType.class), anyMapOf(String.class,
        Object.class), any(EntityManager.class));

    when(sessionContext.getCUDRequestHandler()).thenReturn(handler);

    try {
      processor.deleteEntity(response);
    } catch (ODataApplicationException e) {
      assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @Test
  public void testThrowExpectedExceptionInCaseOfError() throws ODataJPAProcessException {
    ODataResponse response = new ODataResponse();
    JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).deleteEntity(any(JPAEntityType.class), anyMapOf(String.class,
            Object.class), any(EntityManager.class));

    when(sessionContext.getCUDRequestHandler()).thenReturn(handler);

    try {
      processor.deleteEntity(response);
    } catch (ODataApplicationException e) {
      assertEquals(HttpStatusCode.BAD_REQUEST.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @Test
  public void testConvertEntityType() throws ODataJPAProcessException {
    ODataResponse response = new ODataResponse();
    RequestHandleSpy spy = new RequestHandleSpy();
    UriParameter param = mock(UriParameter.class);

    keyPredicates.add(param);

    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    when(param.getName()).thenReturn("ID");
    when(param.getText()).thenReturn("'1'");

    processor.deleteEntity(response);

    assertEquals("com.sap.olingo.jpa.processor.core.testmodel.Organization", spy.et.getInternalName());
  }

  @Test
  public void testConvertKeySingleAttribute() throws ODataJPAProcessException {
    ODataResponse response = new ODataResponse();
    RequestHandleSpy spy = new RequestHandleSpy();
    UriParameter param = mock(UriParameter.class);

    keyPredicates.add(param);

    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);

    when(param.getName()).thenReturn("ID");
    when(param.getText()).thenReturn("'1'");
    processor.deleteEntity(response);

    assertEquals(1, spy.keyPredicates.size());
    assertTrue(spy.keyPredicates.get("iD") instanceof String);
    assertEquals("1", spy.keyPredicates.get("iD"));
  }

  @Test
  public void testConvertKeyTwoAttributes() throws ODataJPAProcessException {
    // BusinessPartnerRole
    ODataResponse response = new ODataResponse();
    RequestHandleSpy spy = new RequestHandleSpy();
    UriParameter param1 = mock(UriParameter.class);
    UriParameter param2 = mock(UriParameter.class);

    when(sessionContext.getCUDRequestHandler()).thenReturn(spy);
    when(ets.getName()).thenReturn("BusinessPartnerRoles");
    when(param1.getName()).thenReturn("BusinessPartnerID");
    when(param1.getText()).thenReturn("'1'");
    when(param2.getName()).thenReturn("RoleCategory");
    when(param2.getText()).thenReturn("'A'");
    keyPredicates.add(param1);
    keyPredicates.add(param2);
    processor.deleteEntity(response);

    assertEquals(2, spy.keyPredicates.size());
    assertTrue(spy.keyPredicates.get("businessPartnerID") instanceof String);
    assertEquals("1", spy.keyPredicates.get("businessPartnerID"));
    assertTrue(spy.keyPredicates.get("roleCategory") instanceof String);
    assertEquals("A", spy.keyPredicates.get("roleCategory"));

  }

  class RequestHandleSpy extends JPAAbstractCUDRequestHandler {
    public Map<String, Object> keyPredicates;
    public JPAEntityType       et;

    @Override
    public void deleteEntity(final JPAEntityType et, final Map<String, Object> keyPredicates, EntityManager em) {
      this.keyPredicates = keyPredicates;
      this.et = et;
    }
  }
}
