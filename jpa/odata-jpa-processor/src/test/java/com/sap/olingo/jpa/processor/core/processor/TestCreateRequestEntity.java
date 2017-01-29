package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

public class TestCreateRequestEntity {
  protected static final String PUNIT_NAME = "org.apache.olingo.jpa";
  protected static EntityManagerFactory emf;
  protected static JPAEdmProvider jpaEdm;
  protected static DataSource ds;

  @BeforeClass
  public static void setupClass() throws ODataException {
    JPAEdmMetadataPostProcessor pP = mock(JPAEdmMetadataPostProcessor.class);

    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
    jpaEdm = new JPAEdmProvider(PUNIT_NAME, emf.getMetamodel(), pP);

  }

  private OData odata;
  private JPACUDRequestProcessor cut;
  private Entity oDataEntity;
  private ServiceMetadata serviceMetadata;
  private JPAODataSessionContextAccess sessionContext;
  private JPAODataRequestContextAccess requestContext;
  private UriInfo uriInfo;
  private UriResourceEntitySet uriEts;
  private EntityManager em;
  private EntityTransaction transaction;
  private JPASerializer serializer;
  private EdmEntitySet ets;
  private List<UriParameter> keyPredicates;
  private JPAConversionHelper convHelper;
  private List<UriResource> pathParts = new ArrayList<UriResource>();

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
    convHelper = new JPAConversionHelper();// mock(JPAConversionHelper.class);
    em = mock(EntityManager.class);
    transaction = mock(EntityTransaction.class);

    when(sessionContext.getEdmProvider()).thenReturn(jpaEdm);
    when(requestContext.getEntityManager()).thenReturn(em);
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(requestContext.getSerializer()).thenReturn(serializer);
    when(uriInfo.getUriResourceParts()).thenReturn(pathParts);
    when(uriEts.getKeyPredicates()).thenReturn(keyPredicates);
    when(uriEts.getEntitySet()).thenReturn(ets);
    when(uriEts.getKind()).thenReturn(UriResourceKind.entitySet);
    when(ets.getName()).thenReturn("AdministrativeDivisions");
    when(em.getTransaction()).thenReturn(transaction);
    cut = new JPACUDRequestProcessor(odata, serviceMetadata, sessionContext, requestContext, convHelper);

  }

  @Test
  public void testCheckDataAndEtCreated() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    assertNotNull(act.getData());
    assertNotNull(act.getEntityType());
  }

  @Test
  public void testCheckEtName() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    assertEquals("AdministrativeDivision", act.getEntityType().getExternalName());
  }

  @Test
  public void testCheckDataHasProperty() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);
    String actValue = (String) act.getData().get("codeID");
    assertNotNull(actValue);
    assertEquals("DE50", actValue);
  }

  @Test
  public void testCheckDeepOneChildResultContainsEntityLink() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    List<Link> navigationLinks = new ArrayList<Link>();
    addNavigationLinkDE501(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    Object actValue = act.getData().get("children");
    assertNotNull("Is null", actValue);
    assertTrue("Wrong type", actValue instanceof List);
  }

  @Test
  public void testCheckDeepOneChildResultContainsEntityLinkSize() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    List<Link> navigationLinks = new ArrayList<Link>();
    addNavigationLinkDE501(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    Object actValue = act.getData().get("children");
    assertEquals("Wrong size", 1, ((List<?>) actValue).size());
  }

  @Test
  public void testCheckDeepOneChildResultContainsEntityLinkEntityType() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    List<Link> navigationLinks = new ArrayList<Link>();
    addNavigationLinkDE501(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    Object actValue = act.getData().get("children");
    assertNotNull(((List<?>) actValue).get(0));
    assertNotNull("Entity type not found", ((JPARequestEntity) ((List<?>) actValue).get(0)).getEntityType());
    assertEquals("Wrong Type", "AdministrativeDivision", ((JPARequestEntity) ((List<?>) actValue).get(0))
        .getEntityType().getExternalName());
  }

  @Test
  public void testCheckDeepOneChildResultContainsEntityLinkData() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    List<Link> navigationLinks = new ArrayList<Link>();
    addNavigationLinkDE501(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    Object actValue = act.getData().get("children");
    assertNotNull(((List<?>) actValue).get(0));
    assertNotNull("Entity type not found", ((JPARequestEntity) ((List<?>) actValue).get(0)).getEntityType());
    Map<String, Object> actData = ((JPARequestEntity) ((List<?>) actValue).get(0)).getData();
    assertNotNull("Data not found", actData);
    assertNotNull("CodeID not found", actData.get("codeID"));
    assertEquals("Value not found", "DE501", actData.get("codeID"));
  }

  @Test
  public void testCheckDeepTwoChildResultContainsEntityLinkSize() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    List<Link> navigationLinks = new ArrayList<Link>();
    addNavigationLinkDE502(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    Object actValue = act.getData().get("children");
    assertEquals("Wrong size", 2, ((List<?>) actValue).size());
  }

  private void addNavigationLinkDE501(List<Link> navigationLinks) {
    addNavigationLink(navigationLinks, "DE501", null);
  }

  private void addNavigationLinkDE502(List<Link> navigationLinks) {

    addNavigationLink(navigationLinks, "DE501", "DE502");
  }

  private void addNavigationLink(List<Link> navigationLinks, String codeValue1, String codeValue2) {
    Link navigationLink = mock(Link.class);
    when(navigationLink.getTitle()).thenReturn("Children");
    navigationLinks.add(navigationLink);
    EntityCollection navigationEntitySet = mock(EntityCollection.class);
    List<Entity> entityCollection = new ArrayList<Entity>();

    Entity navigationEntity1 = mock(Entity.class);
    List<Property> navigationEntityProperties1 = createProperties(codeValue1);
    when(navigationEntity1.getProperties()).thenReturn(navigationEntityProperties1);//
    entityCollection.add(navigationEntity1);

    if (codeValue2 != null) {
      Entity navigationEntity2 = mock(Entity.class);
      List<Property> navigationEntityProperties2 = createProperties(codeValue2);
      when(navigationEntity2.getProperties()).thenReturn(navigationEntityProperties2);//
      entityCollection.add(navigationEntity2);
    }
    when(navigationEntitySet.getEntities()).thenReturn(entityCollection);
    when(navigationLink.getInlineEntitySet()).thenReturn(navigationEntitySet);
  }

  private void createODataEntity(List<Property> properties) {
    oDataEntity = mock(Entity.class);
    when(oDataEntity.getProperties()).thenReturn(properties);
  }

  private List<Property> createProperties() {
    return createProperties("DE50");
  }

  private List<Property> createProperties(String codeID) {
    List<Property> properties = new ArrayList<Property>();
    Property property = mock(Property.class);
    when(property.getName()).thenReturn("CodeID");
    when(property.getValue()).thenReturn(codeID);
    when(property.getValueType()).thenReturn(ValueType.PRIMITIVE);
    properties.add(property);
    return properties;
  }
}
