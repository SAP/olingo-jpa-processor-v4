package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
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
  public void testCreateDataAndEtCreated() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    assertNotNull(act.getData());
    assertNotNull(act.getEntityType());
  }

  @Test
  public void testCreateEtName() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    assertEquals("AdministrativeDivision", act.getEntityType().getExternalName());
  }

  @Test
  public void testCreateDataHasProperty() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);
    String actValue = (String) act.getData().get("codeID");
    assertNotNull(actValue);
    assertEquals("DE50", actValue);
  }

  @Test
  public void testCreateEmptyRelatedEntities() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    assertNotNull(act.getRelatedEntities());
    assertTrue(act.getRelatedEntities().isEmpty());
  }

  @Test
  public void testCreateEmptyRelationLinks() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    assertNotNull(act.getRelationLinks());
    assertTrue(act.getRelationLinks().isEmpty());
  }

  @Test
  public void testCreateDeepOneChildResultContainsEntityLink() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    List<Link> navigationLinks = new ArrayList<Link>();
    addChildrenNavigationLinkDE501(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    Object actValue = findEntitryList(act.getRelatedEntities(), ("children"));
    assertNotNull("Is null", actValue);
    assertTrue("Wrong type", actValue instanceof List);
  }

  @Test
  public void testCreateDeepOneChildResultContainsEntityLinkSize() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    List<Link> navigationLinks = new ArrayList<Link>();
    addChildrenNavigationLinkDE501(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    Object actValue = findEntitryList(act.getRelatedEntities(), ("children"));
    assertEquals("Wrong size", 1, ((List<?>) actValue).size());
  }

  @Test
  public void testCreateDeepOneChildResultContainsEntityLinkEntityType() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    List<Link> navigationLinks = new ArrayList<Link>();
    addChildrenNavigationLinkDE501(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    Object actValue = findEntitryList(act.getRelatedEntities(), ("children"));
    assertNotNull(((List<?>) actValue).get(0));
    assertNotNull("Entity type not found", ((JPARequestEntity) ((List<?>) actValue).get(0)).getEntityType());
    assertEquals("Wrong Type", "AdministrativeDivision", ((JPARequestEntity) ((List<?>) actValue).get(0))
        .getEntityType().getExternalName());
  }

  @Test
  public void testCreateDeepOneChildResultContainsEntityLinkData() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    List<Link> navigationLinks = new ArrayList<Link>();
    addChildrenNavigationLinkDE501(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    Object actValue = findEntitryList(act.getRelatedEntities(), ("children"));
    assertNotNull(((List<?>) actValue).get(0));
    assertNotNull("Entity type not found", ((JPARequestEntity) ((List<?>) actValue).get(0)).getEntityType());
    Map<String, Object> actData = ((JPARequestEntity) ((List<?>) actValue).get(0)).getData();
    assertNotNull("Data not found", actData);
    assertNotNull("CodeID not found", actData.get("codeID"));
    assertEquals("Value not found", "DE501", actData.get("codeID"));
  }

  @Test
  public void testCreateDeepTwoChildResultContainsEntityLinkSize() throws ODataJPAModelException, ODataException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    List<Link> navigationLinks = new ArrayList<Link>();
    addNavigationLinkDE502(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    Object actValue = findEntitryList(act.getRelatedEntities(), ("children"));
    assertEquals("Wrong size", 2, ((List<?>) actValue).size());
  }

  @Test
  public void testCreateWithLinkToOne() throws ODataJPAProcessorException {
    List<Property> properties = createProperties();
    createODataEntity(properties);
    List<Link> bindingLinks = new ArrayList<Link>();
    addParentBindingLink(bindingLinks);
    when(oDataEntity.getNavigationBindings()).thenReturn(bindingLinks);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    Object actValue = findLinkList(act.getRelationLinks(), ("parent"));
    assertNotNull(actValue);
    assertTrue(actValue instanceof List<?>);
  }

  @Test
  public void testCreateWithLinkToMany() throws ODataJPAProcessorException {
    List<Property> properties = createProperties();
    createODataEntity(properties);
    List<Link> bindingLinks = new ArrayList<Link>();
    addChildrenBindingLink(bindingLinks);
    when(oDataEntity.getNavigationBindings()).thenReturn(bindingLinks);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    Object actValue = findLinkList(act.getRelationLinks(), ("children"));
    assertNotNull(actValue);
    assertTrue(actValue instanceof List<?>);
    assertEquals(2, ((List<?>) actValue).size());
  }

  @Test
  public void testCreateDeepToOne() throws ODataJPAProcessorException {
    List<Property> properties = createProperties();
    createODataEntity(properties);

    List<Link> navigationLinks = new ArrayList<Link>();
    addParentNavigationLink(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    Object actValue = findEntitryList(act.getRelatedEntities(), ("parent"));
    assertNotNull(actValue);
    assertTrue(actValue instanceof List<?>);
  }

  @Test
  public void testCreateOrgWithRoles() throws ODataJPAProcessorException {
    List<Property> properties = new ArrayList<Property>();
    Property property = mock(Property.class);
    when(property.getName()).thenReturn("ID");
    when(property.getValue()).thenReturn("20");
    when(property.getValueType()).thenReturn(ValueType.PRIMITIVE);
    properties.add(property);
    createODataEntity(properties);
//-----------------------------
    List<Link> navigationLinks = new ArrayList<Link>();

    Link navigationLink = mock(Link.class);
    when(navigationLink.getTitle()).thenReturn("Roles");
    navigationLinks.add(navigationLink);
    EntityCollection navigationEntitySet = mock(EntityCollection.class);
    List<Entity> entityCollection = new ArrayList<Entity>();

    Entity navigationEntity1 = mock(Entity.class);
    List<Property> navigationEntityProperties1 = createPropertiesRoles("20", "A");
    when(navigationEntity1.getProperties()).thenReturn(navigationEntityProperties1);//
    entityCollection.add(navigationEntity1);

    Entity navigationEntity2 = mock(Entity.class);
    List<Property> navigationEntityProperties2 = createPropertiesRoles("20", "C");
    when(navigationEntity2.getProperties()).thenReturn(navigationEntityProperties2);//
    entityCollection.add(navigationEntity2);

    when(navigationEntitySet.getEntities()).thenReturn(entityCollection);
    when(navigationLink.getInlineEntitySet()).thenReturn(navigationEntitySet);

    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);
//------------------------------------
    when(ets.getName()).thenReturn("Organizations");
    JPARequestEntity act = cut.createRequestEntity(ets, oDataEntity);

    assertNotNull(act);
    assertNotNull(act.getData());
    assertNotNull(findEntitryList(act.getRelatedEntities(), ("roles")));
  }

  private void addParentNavigationLink(List<Link> navigationLinks) {
    Link navigationLink = mock(Link.class);
    when(navigationLink.getTitle()).thenReturn("Parent");
    navigationLinks.add(navigationLink);
    Entity navigationEntity = mock(Entity.class);
    when(navigationLink.getInlineEntity()).thenReturn(navigationEntity);
    List<Property> navigationEntityProperties = createPropertyCodeID("DE5");
    when(navigationEntity.getProperties()).thenReturn(navigationEntityProperties);//
  }

  private void addParentBindingLink(List<Link> bindingLinks) {
    Link bindingLink = mock(Link.class);
    when(bindingLink.getTitle()).thenReturn("Parent");
    bindingLinks.add(bindingLink);
    when(bindingLink.getBindingLink()).thenReturn(
        "AdministrativeDivisions(DivisionCode='DE1',CodeID='NUTS1',CodePublisher='Eurostat')");
  }

  private void addChildrenNavigationLinkDE501(List<Link> navigationLinks) {
    addChildrenNavigationLink(navigationLinks, "DE501", null);
  }

  private void addNavigationLinkDE502(List<Link> navigationLinks) {

    addChildrenNavigationLink(navigationLinks, "DE501", "DE502");
  }

  private void addChildrenNavigationLink(List<Link> navigationLinks, String codeValue1, String codeValue2) {
    Link navigationLink = mock(Link.class);
    when(navigationLink.getTitle()).thenReturn("Children");
    navigationLinks.add(navigationLink);
    EntityCollection navigationEntitySet = mock(EntityCollection.class);
    List<Entity> entityCollection = new ArrayList<Entity>();

    Entity navigationEntity1 = mock(Entity.class);
    List<Property> navigationEntityProperties1 = createPropertyCodeID(codeValue1);
    when(navigationEntity1.getProperties()).thenReturn(navigationEntityProperties1);//
    entityCollection.add(navigationEntity1);

    if (codeValue2 != null) {
      Entity navigationEntity2 = mock(Entity.class);
      List<Property> navigationEntityProperties2 = createPropertyCodeID(codeValue2);
      when(navigationEntity2.getProperties()).thenReturn(navigationEntityProperties2);//
      entityCollection.add(navigationEntity2);
    }
    when(navigationEntitySet.getEntities()).thenReturn(entityCollection);
    when(navigationLink.getInlineEntitySet()).thenReturn(navigationEntitySet);
  }

  private void addChildrenBindingLink(List<Link> bindingLinks) {
    List<String> links = new ArrayList<String>();

    Link bindingLink = mock(Link.class);
    when(bindingLink.getTitle()).thenReturn("Children");
    bindingLinks.add(bindingLink);
    when(bindingLink.getBindingLinks()).thenReturn(links);
    links.add("AdministrativeDivisions(DivisionCode='DE100',CodeID='NUTS3',CodePublisher='Eurostat')");
    links.add("AdministrativeDivisions(DivisionCode='DE101',CodeID='NUTS3',CodePublisher='Eurostat')");
  }

  private void createODataEntity(List<Property> properties) {
    oDataEntity = mock(Entity.class);
    when(oDataEntity.getProperties()).thenReturn(properties);
  }

  private List<Property> createProperties() {
    return createPropertyCodeID("DE50");
  }

  private List<Property> createPropertyCodeID(String codeID) {
    List<Property> properties = new ArrayList<Property>();
    Property property = mock(Property.class);
    when(property.getName()).thenReturn("CodeID");
    when(property.getValue()).thenReturn(codeID);
    when(property.getValueType()).thenReturn(ValueType.PRIMITIVE);
    properties.add(property);
    return properties;
  }

  private List<Property> createPropertiesRoles(String BuPaId, String RoleCategory) {
    List<Property> properties = new ArrayList<Property>();
    Property propertyId = mock(Property.class);
    when(propertyId.getName()).thenReturn("BusinessPartnerID");
    when(propertyId.getValue()).thenReturn(BuPaId);
    when(propertyId.getValueType()).thenReturn(ValueType.PRIMITIVE);
    properties.add(propertyId);
    Property propertyCategory = mock(Property.class);
    when(propertyCategory.getName()).thenReturn("RoleCategory");
    when(propertyCategory.getValue()).thenReturn(RoleCategory);
    when(propertyCategory.getValueType()).thenReturn(ValueType.PRIMITIVE);
    properties.add(propertyCategory);
    return properties;
  }

  private Object findEntitryList(Map<JPAAssociationPath, List<JPARequestEntity>> relatedEntities,
      String assoziationName) {
    for (Entry<JPAAssociationPath, List<JPARequestEntity>> entity : relatedEntities.entrySet()) {
      if (entity.getKey().getPath().get(0).getInternalName().equals(assoziationName))
        return entity.getValue();
    }
    return null;
  }

  private Object findLinkList(Map<JPAAssociationPath, List<JPARequestLink>> relationLink, String assoziationName) {
    for (Entry<JPAAssociationPath, List<JPARequestLink>> entity : relationLink.entrySet()) {
      if (entity.getKey().getPath().get(0).getInternalName().equals(assoziationName))
        return entity.getValue();
    }
    return null;
  }

}
