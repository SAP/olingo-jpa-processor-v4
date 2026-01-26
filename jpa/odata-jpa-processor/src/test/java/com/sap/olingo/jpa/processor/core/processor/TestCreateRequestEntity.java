package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import org.apache.olingo.commons.api.data.ComplexValue;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.JPAReferences;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;
import com.sap.olingo.jpa.processor.core.query.EdmBindingTargetInfo;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestWrapperChecker;

class TestCreateRequestEntity {
  protected static final String ENTITY_SET_NAME = "AdministrativeDivisions";
  protected static final String ENTITY_TYPE_NAME = "AdministrativeDivision";
  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  protected static EntityManagerFactory emf;
  protected static JPAEdmProvider jpaEdm;
  protected static DataSource dataSource;
  protected static List<AnnotationProvider> annotationProvider;
  protected static JPAReferences references;
  protected static EdmBindingTargetInfo targetInfo;

  @BeforeAll
  static void setupClass() throws ODataException {
    final JPAEdmMetadataPostProcessor postProcessor = mock(JPAEdmMetadataPostProcessor.class);
    targetInfo = mock(EdmBindingTargetInfo.class);
    annotationProvider = new ArrayList<>();

    dataSource = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, dataSource);
    jpaEdm = new JPAEdmProvider(emf.getMetamodel(), new TestWrapperChecker(), postProcessor, TestBase.enumPackages,
        new JPADefaultEdmNameBuilder(PUNIT_NAME), annotationProvider);
  }

  private OData odata;
  private JPACUDRequestProcessor cut;
  private Entity oDataEntity;
  private ServiceMetadata serviceMetadata;
  private JPAODataRequestContextAccess requestContext;
  private UriInfo uriInfo;
  private UriResourceEntitySet uriEs;
  private EntityManager em;
  private EntityTransaction transaction;
  private JPASerializer serializer;
  private EdmEntitySet es;
  private List<UriParameter> keyPredicates;
  private JPAConversionHelper convHelper;
  private final List<UriResource> pathParts = new ArrayList<>();
  private Map<String, List<String>> headers;

  @BeforeEach
  void setUp() throws Exception {
    odata = OData.newInstance();
    requestContext = mock(JPAODataRequestContextAccess.class);
    serviceMetadata = mock(ServiceMetadata.class);
    uriInfo = mock(UriInfo.class);
    keyPredicates = new ArrayList<>();
    es = mock(EdmEntitySet.class);
    serializer = mock(JPASerializer.class);
    uriEs = mock(UriResourceEntitySet.class);
    pathParts.add(uriEs);
    convHelper = new JPAConversionHelper();// mock(JPAConversionHelper.class);
    em = mock(EntityManager.class);
    transaction = mock(EntityTransaction.class);
    headers = new HashMap<>(0);

    when(requestContext.getEdmProvider()).thenReturn(jpaEdm);
    when(requestContext.getEntityManager()).thenReturn(em);
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(requestContext.getSerializer()).thenReturn(serializer);
    when(uriInfo.getUriResourceParts()).thenReturn(pathParts);
    when(uriEs.getKeyPredicates()).thenReturn(keyPredicates);
    when(uriEs.getEntitySet()).thenReturn(es);
    when(uriEs.getKind()).thenReturn(UriResourceKind.entitySet);
    when(es.getName()).thenReturn(ENTITY_SET_NAME);
    when(em.getTransaction()).thenReturn(transaction);
    when(targetInfo.getEdmBindingTarget()).thenReturn(es);
    when(targetInfo.getName()).thenReturn(ENTITY_SET_NAME);
    when(targetInfo.getKeyPredicates()).thenReturn(Collections.emptyList());
    cut = new JPACUDRequestProcessor(odata, serviceMetadata, requestContext, convHelper);

  }

  @Test
  void testCreateDataAndEtCreated() throws ODataException {
    final List<Property> properties = createProperties();
    createODataEntity(properties);

    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);

    assertNotNull(act.getData());
    assertNotNull(act.getEntityType());
  }

  @Test
  void testCreateEtName() throws ODataException {
    final List<Property> properties = createProperties();
    createODataEntity(properties);

    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);

    assertEquals(ENTITY_TYPE_NAME, act.getEntityType().getExternalName());
  }

  @Test
  void testCreateDataHasProperty() throws ODataException {
    final List<Property> properties = createProperties();
    createODataEntity(properties);

    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);
    final String actValue = (String) act.getData().get("codeID");
    assertNotNull(actValue);
    assertEquals("DE50", actValue);
  }

  @Test
  void testCreateEmptyRelatedEntities() throws ODataException {
    final List<Property> properties = createProperties();
    createODataEntity(properties);

    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);

    assertNotNull(act.getRelatedEntities());
    assertTrue(act.getRelatedEntities().isEmpty());
  }

  @Test
  void testCreateEmptyRelationLinks() throws ODataException {
    final List<Property> properties = createProperties();
    createODataEntity(properties);

    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);

    assertNotNull(act.getRelationLinks());
    assertTrue(act.getRelationLinks().isEmpty());
  }

  @Test
  void testCreateDeepOneChildResultContainsEntityLink() throws ODataException {
    final List<Property> properties = createProperties();
    createODataEntity(properties);

    final List<Link> navigationLinks = new ArrayList<>();
    addChildrenNavigationLinkDE501(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);

    final Object actValue = findEntryList(act.getRelatedEntities(), ("children"));
    assertNotNull(actValue, "Is null");
    assertTrue(actValue instanceof List, "Wrong type");
  }

  @Test
  void testCreateDeepOneChildResultContainsEntityLinkSize() throws ODataException {
    final List<Property> properties = createProperties();
    createODataEntity(properties);

    final List<Link> navigationLinks = new ArrayList<>();
    addChildrenNavigationLinkDE501(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);

    final Object actValue = findEntryList(act.getRelatedEntities(), ("children"));
    assertEquals(1, ((List<?>) actValue).size(), "Wrong size");
  }

  @Test
  void testCreateDeepOneChildResultContainsEntityLinkEntityType() throws ODataException {
    final List<Property> properties = createProperties();
    createODataEntity(properties);

    final List<Link> navigationLinks = new ArrayList<>();
    addChildrenNavigationLinkDE501(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);

    final Object actValue = findEntryList(act.getRelatedEntities(), ("children"));
    assertNotNull(((List<?>) actValue).get(0));
    assertNotNull(((JPARequestEntity) ((List<?>) actValue).get(0)).getEntityType(), "Entity type not found");
    assertEquals("AdministrativeDivision", ((JPARequestEntity) ((List<?>) actValue).get(0))
        .getEntityType().getExternalName(), "Wrong Type");
  }

  @Test
  void testCreateDeepOneChildResultContainsEntityLinkData() throws ODataException {
    final List<Property> properties = createProperties();
    createODataEntity(properties);

    final List<Link> navigationLinks = new ArrayList<>();
    addChildrenNavigationLinkDE501(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);

    final Object actValue = findEntryList(act.getRelatedEntities(), ("children"));
    assertNotNull(((List<?>) actValue).get(0));
    assertNotNull(((JPARequestEntity) ((List<?>) actValue).get(0)).getEntityType(), "Entity type not found");
    final Map<String, Object> actData = ((JPARequestEntity) ((List<?>) actValue).get(0)).getData();
    assertNotNull(actData, "Data not found");
    assertNotNull(actData.get("codeID"), "CodeID not found");
    assertEquals("DE501", actData.get("codeID"), "Value not found");
  }

  @Test
  void testCreateDeepTwoChildResultContainsEntityLinkSize() throws ODataException {
    final List<Property> properties = createProperties();
    createODataEntity(properties);

    final List<Link> navigationLinks = new ArrayList<>();
    addNavigationLinkDE502(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);

    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);

    final Object actValue = findEntryList(act.getRelatedEntities(), ("children"));
    assertEquals(2, ((List<?>) actValue).size(), "Wrong size");
  }

  @Test
  void testCreateWithLinkToOne() throws ODataJPAProcessorException {
    final List<Property> properties = createProperties();
    createODataEntity(properties);
    final List<Link> bindingLinks = new ArrayList<>();
    addParentBindingLink(bindingLinks);
    when(oDataEntity.getNavigationBindings()).thenReturn(bindingLinks);

    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);

    final Object actValue = findLinkList(act.getRelationLinks(), ("parent"));
    assertNotNull(actValue);
    assertTrue(actValue instanceof List<?>);
  }

  @Test
  void testCreateWithLinkToMany() throws ODataJPAProcessorException {
    final List<Property> properties = createProperties();
    createODataEntity(properties);
    final List<Link> bindingLinks = new ArrayList<>();
    addChildrenBindingLink(bindingLinks);
    when(oDataEntity.getNavigationBindings()).thenReturn(bindingLinks);

    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);

    final Object actValue = findLinkList(act.getRelationLinks(), ("children"));
    assertNotNull(actValue);
    assertTrue(actValue instanceof List<?>);
    assertEquals(2, ((List<?>) actValue).size());
  }

  @Test
  void testCreateDeepToOne() throws ODataJPAProcessorException {
    final List<Property> properties = createProperties();
    createODataEntity(properties);

    final List<Link> navigationLinks = new ArrayList<>();
    final Link navigationLink = addParentNavigationLink(navigationLinks);
    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);
    when(oDataEntity.getNavigationLink("Parent")).thenReturn(navigationLink);

    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);

    final Object actValue = findEntryList(act.getRelatedEntities(), ("parent"));
    assertNotNull(actValue);
    assertTrue(actValue instanceof List<?>);
  }

  @Test
  void testCreateOrganizationWithRoles() throws ODataJPAProcessorException {

    final List<Property> properties = new ArrayList<>();
    createPropertyBuPaID(properties, "20");
    createODataEntity(properties);
//-----------------------------
    final List<Link> navigationLinks = new ArrayList<>();

    final Link navigationLink = mock(Link.class);
    when(navigationLink.getTitle()).thenReturn("Roles");
    navigationLinks.add(navigationLink);
    final EntityCollection navigationEntitySet = mock(EntityCollection.class);
    final List<Entity> entityCollection = new ArrayList<>();

    final Entity navigationEntity1 = mock(Entity.class);
    final List<Property> navigationEntityProperties1 = createPropertiesRoles("20", "A");
    when(navigationEntity1.getProperties()).thenReturn(navigationEntityProperties1);//
    entityCollection.add(navigationEntity1);

    final Entity navigationEntity2 = mock(Entity.class);
    final List<Property> navigationEntityProperties2 = createPropertiesRoles("20", "C");
    when(navigationEntity2.getProperties()).thenReturn(navigationEntityProperties2);//
    entityCollection.add(navigationEntity2);

    when(navigationEntitySet.getEntities()).thenReturn(entityCollection);
    when(navigationLink.getInlineEntitySet()).thenReturn(navigationEntitySet);

    when(oDataEntity.getNavigationLinks()).thenReturn(navigationLinks);
    when(oDataEntity.getNavigationLink("Roles")).thenReturn(navigationLink);
//------------------------------------
    when(es.getName()).thenReturn("Organizations");
    when(targetInfo.getName()).thenReturn("Organizations");
    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);

    assertNotNull(act);
    assertNotNull(act.getData());
    assertNotNull(findEntryList(act.getRelatedEntities(), ("roles")));
  }

  @Test
  void testCreateDeepOneChildViaComplex() throws ODataException {
    final List<Property> properties = new ArrayList<>();
    final List<Property> inlineProperties = new ArrayList<>();
    final Entity inlineEntity = mock(Entity.class);

    createODataEntity(properties);

    when(targetInfo.getName()).thenReturn("Persons");
    when(es.getName()).thenReturn("Persons");
    createPropertyBuPaID(properties, "20");
    when(inlineEntity.getProperties()).thenReturn(inlineProperties);
    createPropertyBuPaID(inlineProperties, "200");

    final List<Property> adminProperties = createComplexProperty(properties, "AdministrativeInformation", null, null,
        oDataEntity);
    final List<Property> createdProperties = createComplexProperty(adminProperties, "Created", "User", inlineEntity,
        oDataEntity);
    createPrimitiveProperty(createdProperties, "99", "By");
    createPrimitiveProperty(createdProperties, Timestamp.valueOf("2016-01-20 09:21:23.0"), "At");

    final JPARequestEntity act = cut.createRequestEntity(targetInfo, oDataEntity, headers);
    final Object actValue = findEntryList(act.getRelatedEntities(), ("administrativeInformation"));

    assertNotNull(actValue);
    assertNotNull(((List<?>) actValue).get(0));
    @SuppressWarnings("unchecked")
    final JPARequestEntity actDeepEntity = ((List<JPARequestEntity>) actValue).get(0);
    assertEquals("200", actDeepEntity.getData().get("iD"));
  }

  private List<Property> createComplexProperty(final List<Property> properties, final String name, final String target,
      final Entity inlineEntity, final Entity oDataEntity) {

    final Property property = mock(Property.class);
    final ComplexValue cv = mock(ComplexValue.class);
    final List<Property> cProperties = new ArrayList<>();
    final Link navigationLink = mock(Link.class);

    when(property.getName()).thenReturn(name);
    when(property.getValue()).thenReturn(cv);
    when(property.getValueType()).thenReturn(ValueType.COMPLEX);
    when(property.isComplex()).thenReturn(true);
    when(property.asComplex()).thenReturn(cv);

    when(cv.getValue()).thenReturn(cProperties);
    when(cv.getNavigationLink(target)).thenReturn(navigationLink);
    when(navigationLink.getInlineEntity()).thenReturn(inlineEntity);

    when(oDataEntity.getProperty(name)).thenReturn(property);

    properties.add(property);
    return cProperties;
  }

  private Link addParentNavigationLink(final List<Link> navigationLinks) {

    final Link navigationLink = mock(Link.class);
    when(navigationLink.getTitle()).thenReturn("Parent");
    navigationLinks.add(navigationLink);
    final Entity navigationEntity = mock(Entity.class);
    when(navigationLink.getInlineEntity()).thenReturn(navigationEntity);
    final List<Property> navigationEntityProperties = createPropertyCodeID("DE5");
    when(navigationEntity.getProperties()).thenReturn(navigationEntityProperties);//
    return navigationLink;
  }

  private void addParentBindingLink(final List<Link> bindingLinks) {
    final Link bindingLink = mock(Link.class);
    when(bindingLink.getTitle()).thenReturn("Parent");
    bindingLinks.add(bindingLink);
    when(bindingLink.getBindingLink()).thenReturn(
        "AdministrativeDivisions(DivisionCode='DE1',CodeID='NUTS1',CodePublisher='Eurostat')");
  }

  private void addChildrenNavigationLinkDE501(final List<Link> navigationLinks) {
    addChildrenNavigationLink(navigationLinks, "DE501", null);
  }

  private void addNavigationLinkDE502(final List<Link> navigationLinks) {

    addChildrenNavigationLink(navigationLinks, "DE501", "DE502");
  }

  private void addChildrenNavigationLink(final List<Link> navigationLinks, final String codeValue1,
      final String codeValue2) {
    final Link navigationLink = mock(Link.class);
    when(navigationLink.getTitle()).thenReturn("Children");
    navigationLinks.add(navigationLink);
    final EntityCollection navigationEntitySet = mock(EntityCollection.class);
    final List<Entity> entityCollection = new ArrayList<>();

    final Entity navigationEntity1 = mock(Entity.class);
    final List<Property> navigationEntityProperties1 = createPropertyCodeID(codeValue1);
    when(navigationEntity1.getProperties()).thenReturn(navigationEntityProperties1);//
    entityCollection.add(navigationEntity1);
    if (codeValue2 != null) {
      final Entity navigationEntity2 = mock(Entity.class);
      final List<Property> navigationEntityProperties2 = createPropertyCodeID(codeValue2);
      when(navigationEntity2.getProperties()).thenReturn(navigationEntityProperties2);//
      entityCollection.add(navigationEntity2);
    }
    when(navigationEntitySet.getEntities()).thenReturn(entityCollection);
    when(navigationLink.getInlineEntitySet()).thenReturn(navigationEntitySet);
    when(oDataEntity.getNavigationLink("Children")).thenReturn(navigationLink);

  }

  private void addChildrenBindingLink(final List<Link> bindingLinks) {
    final List<String> links = new ArrayList<>();

    final Link bindingLink = mock(Link.class);
    when(bindingLink.getTitle()).thenReturn("Children");
    bindingLinks.add(bindingLink);
    when(bindingLink.getBindingLinks()).thenReturn(links);
    links.add("AdministrativeDivisions(DivisionCode='DE100',CodeID='NUTS3',CodePublisher='Eurostat')");
    links.add("AdministrativeDivisions(DivisionCode='DE101',CodeID='NUTS3',CodePublisher='Eurostat')");
  }

  private void createODataEntity(final List<Property> properties) {
    oDataEntity = mock(Entity.class);
    when(oDataEntity.getProperties()).thenReturn(properties);
  }

  private List<Property> createProperties() {
    return createPropertyCodeID("DE50");
  }

  private List<Property> createPropertyCodeID(final String codeID) {
    final List<Property> properties = new ArrayList<>();
    createPrimitiveProperty(properties, codeID, "CodeID");
    return properties;
  }

  private void createPropertyBuPaID(final List<Property> properties, final String value) {

    createPrimitiveProperty(properties, value, "ID");
  }

  private void createPrimitiveProperty(final List<Property> properties, final Object value, final String name) {

    final Property property = mock(Property.class);
    when(property.getName()).thenReturn(name);
    when(property.getValue()).thenReturn(value);
    when(property.getValueType()).thenReturn(ValueType.PRIMITIVE);
    properties.add(property);
  }

  private List<Property> createPropertiesRoles(final String BuPaId, final String RoleCategory) {
    final List<Property> properties = new ArrayList<>();
    createPrimitiveProperty(properties, BuPaId, "BusinessPartnerID");
    createPrimitiveProperty(properties, RoleCategory, "RoleCategory");
    return properties;
  }

  private Object findEntryList(final Map<JPAAssociationPath, List<JPARequestEntity>> relatedEntities,
      final String associationName) {
    for (final Entry<JPAAssociationPath, List<JPARequestEntity>> entity : relatedEntities.entrySet()) {
      if (entity.getKey().getPath().get(0).getInternalName().equals(associationName))
        return entity.getValue();
    }
    return null;
  }

  private Object findLinkList(final Map<JPAAssociationPath, List<JPARequestLink>> relationLink,
      final String associationName) {
    for (final Entry<JPAAssociationPath, List<JPARequestLink>> entity : relationLink.entrySet()) {
      if (entity.getKey().getPath().get(0).getInternalName().equals(associationName))
        return entity.getValue();
    }
    return null;
  }

}
