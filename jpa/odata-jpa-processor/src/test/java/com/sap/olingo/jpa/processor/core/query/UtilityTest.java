package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmElement;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmSingleton;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceSingleton;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAUtilException;
import com.sap.olingo.jpa.processor.core.testmodel.CurrentUser;
import com.sap.olingo.jpa.processor.core.testmodel.Singleton;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

class UtilityTest extends TestBase {
  private UriInfoResource uriInfo;
  private List<UriResource> resourceParts;

  @BeforeEach
  void setUp() throws ODataException {
    getHelper();
    uriInfo = mock(UriInfoResource.class);
    resourceParts = new ArrayList<>();
  }

  @Test
  void testDetermineNavigationPathReturnsSingleton() throws ODataApplicationException {
    final UriResourceSingleton resourcePart = mock(UriResourceSingleton.class);
    final EdmType edmType = mock(EdmType.class);
    when(resourcePart.getKind()).thenReturn(UriResourceKind.singleton);
    when(resourcePart.getType()).thenReturn(edmType);
    when(edmType.getNamespace()).thenReturn(PUNIT_NAME);
    when(edmType.getName()).thenReturn(CurrentUser.class.getSimpleName());

    resourceParts.add(resourcePart);
    final List<JPANavigationPropertyInfo> act = Utility.determineNavigationPath(helper.sd, resourceParts, uriInfo);
    assertNotNull(act);
    assertEquals(1, act.size());
  }

  @Test
  void testDetermineAssociationPathCreatesPathForSingleton() throws ODataApplicationException {
    final StringBuilder associationName = new StringBuilder("Details");
    final UriResourceSingleton source = mock(UriResourceSingleton.class);
    final EdmEntityType singletonType = mock(EdmEntityType.class);
    when(singletonType.getNamespace()).thenReturn(PUNIT_NAME);
    when(singletonType.getName()).thenReturn(Singleton.class.getSimpleName());
    when(source.getType()).thenReturn(singletonType);

    final JPAAssociationPath act = Utility.determineAssociationPath(helper.sd, source, associationName);
    assertNotNull(act);
  }

  @Test
  void testDetermineAssociationThrowExceptionOnStartNull() throws ODataJPAModelException {
    final JPAServiceDocument sd = mock(JPAServiceDocument.class);
    final EdmType navigationStart = mock(EdmType.class);
    when(sd.getEntity(navigationStart)).thenReturn(null);
    assertThrows(ODataJPAUtilException.class, () -> Utility.determineAssociation(sd, navigationStart, new StringBuilder(
        "Start")));
  }

  @Test
  void testDetermineAssociationReThrowsException() throws ODataJPAModelException {
    final JPAServiceDocument sd = mock(JPAServiceDocument.class);
    final EdmType navigationStart = mock(EdmType.class);
    when(sd.getEntity(navigationStart)).thenThrow(ODataJPAModelException.class);
    assertThrows(ODataJPAUtilException.class, () -> Utility.determineAssociation(sd, navigationStart, new StringBuilder(
        "Start")));
  }

  @Test
  void testDetermineKeyPredicatesEntitySet() throws ODataApplicationException {

    final UriResourceEntitySet es = mock(UriResourceEntitySet.class);
    final UriParameter keyElement = mock(UriParameter.class);
    final List<UriParameter> keys = Collections.singletonList(keyElement);
    when(es.getKeyPredicates()).thenReturn(keys);
    final List<UriParameter> act = Utility.determineKeyPredicates(es);
    assertEquals(keys, act);
  }

  @Test
  void testDetermineKeyPredicatesNavigation() throws ODataApplicationException {

    final UriResourceNavigation navigation = mock(UriResourceNavigation.class);
    final UriParameter keyElement = mock(UriParameter.class);
    final List<UriParameter> keys = Collections.singletonList(keyElement);
    when(navigation.getKeyPredicates()).thenReturn(keys);
    final List<UriParameter> act = Utility.determineKeyPredicates(navigation);
    assertEquals(keys, act);
  }

  @Test
  void testDetermineKeySingleton() throws ODataApplicationException {

    final UriResourceSingleton singleton = mock(UriResourceSingleton.class);
    final List<UriParameter> act = Utility.determineKeyPredicates(singleton);
    assertTrue(act.isEmpty());
  }

  @Test
  void testDetermineModifyEntitySetAndKeysOnlyEntitySet() {

    final EdmEntitySet es = mock(EdmEntitySet.class);
    final UriResourceEntitySet resourceItem = createEntitySetResource(es);

    final List<UriResource> resources = Arrays.asList(resourceItem);

    final EdmBindingTargetInfo act = Utility.determineModifyEntitySetAndKeys(resources);
    assertEquals(es, act.getEdmBindingTarget());
    assertEquals("", act.getNavigationPath());
    assertEquals(0, act.getKeyPredicates().size());
  }

  @Test
  void testDetermineModifyEntitySetAndKeysOnlyEntitySetKey() {

    final EdmEntitySet es = mock(EdmEntitySet.class);
    final UriResourceEntitySet resourceItem = createEntitySetResource(es);
    final UriParameter keyParameter = mock(UriParameter.class);
    final List<UriParameter> keyParameters = Arrays.asList(keyParameter);
    when(resourceItem.getKeyPredicates()).thenReturn(keyParameters);

    final List<UriResource> resources = Arrays.asList(resourceItem);

    final EdmBindingTargetInfo act = Utility.determineModifyEntitySetAndKeys(resources);
    assertEquals(es, act.getEdmBindingTarget());
    assertEquals("", act.getNavigationPath());
    assertEquals(1, act.getKeyPredicates().size());
  }

  @Test
  void testDetermineModifyEntitySetAndKeysPlusNavigation() {

    final EdmEntitySet es = mock(EdmEntitySet.class);
    final UriResourceEntitySet entitySet = createEntitySetResource(es);
    final UriResourceNavigation navigation = createNavigationResource();
    final List<UriResource> resources = Arrays.asList(entitySet, navigation);

    final EdmBindingTargetInfo act = Utility.determineModifyEntitySetAndKeys(resources);
    assertEquals(es, act.getEdmBindingTarget());
    assertEquals("Navigation", act.getNavigationPath());
    assertEquals(0, act.getKeyPredicates().size());
  }

  @Test
  void testDetermineModifyEntitySetAndKeysPlusNavigationKey() {

    final EdmEntitySet es = mock(EdmEntitySet.class);
    final UriResourceEntitySet entitySet = createEntitySetResource(es);
    final UriResourceNavigation navigation = createNavigationResource();
    final List<UriResource> resources = Arrays.asList(entitySet, navigation);

    final UriParameter keyParameter = mock(UriParameter.class);
    final List<UriParameter> keyParameters = Arrays.asList(keyParameter);
    when(navigation.getKeyPredicates()).thenReturn(keyParameters);

    final EdmBindingTargetInfo act = Utility.determineModifyEntitySetAndKeys(resources);
    assertEquals(es, act.getEdmBindingTarget());
    assertEquals("", act.getNavigationPath());
    assertEquals(1, act.getKeyPredicates().size());
  }

  @Test
  void testDetermineModifyEntitySetAndKeysPlusNavigationKeyTarget() {
    final EdmEntitySet es = mock(EdmEntitySet.class);
    final UriResourceEntitySet entitySet = createEntitySetResource(es);
    final UriResourceNavigation navigation = createNavigationResource();
    final List<UriResource> resources = Arrays.asList(entitySet, navigation);

    final UriParameter keyParameter = mock(UriParameter.class);
    final List<UriParameter> keyParameters = Arrays.asList(keyParameter);
    when(navigation.getKeyPredicates()).thenReturn(keyParameters);
    final EdmEntitySet newTarget = mock(EdmEntitySet.class);
    when(es.getRelatedBindingTarget("Navigation")).thenReturn(newTarget);

    final EdmBindingTargetInfo act = Utility.determineModifyEntitySetAndKeys(resources);
    assertEquals(newTarget, act.getEdmBindingTarget());
    assertEquals("", act.getNavigationPath());
    assertEquals(1, act.getKeyPredicates().size());
  }

  @Test
  void testDetermineModifyEntitySetSingleton() {
    final EdmSingleton single = mock(EdmSingleton.class);
    final UriResourceSingleton resourceItem = createSingletonResource(single);

    final List<UriResource> resources = Arrays.asList(resourceItem);

    final EdmBindingTargetInfo act = Utility.determineModifyEntitySetAndKeys(resources);
    assertEquals(single, act.getEdmBindingTarget());
    assertEquals("", act.getNavigationPath());
    assertEquals(0, act.getKeyPredicates().size());
  }

  @Test
  void testDetermineModifyEntitySetSingletonPlusNavigation() {
    final EdmSingleton single = mock(EdmSingleton.class);
    final UriResourceSingleton resourceItem = createSingletonResource(single);
    final UriResourceNavigation navigation = createNavigationResource();
    final List<UriResource> resources = Arrays.asList(resourceItem, navigation);

    final EdmBindingTargetInfo act = Utility.determineModifyEntitySetAndKeys(resources);
    assertEquals(single, act.getEdmBindingTarget());
    assertEquals("Navigation", act.getNavigationPath());
    assertEquals(0, act.getKeyPredicates().size());
  }

  @Test
  void testDetermineAssociationsNavigationPathAndStar() throws ODataException {
    final TestHelper helper = getHelper();
    final ExpandOption expandOption = mock(ExpandOption.class);
    final ExpandItem expandItem = mock(ExpandItem.class);
    when(expandItem.isStar()).thenReturn(Boolean.TRUE);
    when(expandOption.getExpandItems()).thenReturn(Collections.singletonList(expandItem));

    final EdmNavigationProperty user = createNavigationProperty("User");
    final EdmComplexType created = createComplexType("Created", "User", user);
    final EdmComplexType updated = createComplexType("Updated", "User", user);
    final EdmComplexType administrativeInformation = createComplexType("AdministrativeInformation", new Property(
        "Created", created), new Property("Updated", updated));
    final EdmEntityType person = createEntityType("Person", new Property("AdministrativeInformation",
        administrativeInformation));
    final EdmEntitySet persons = createEntitySet("Persons", person);
    final UriResourceEntitySet es = createEntitySetResource(persons);
    final UriResourceComplexProperty property = createComplexPropertyResource("AdministrativeInformation");

    when(property.getComplexType()).thenReturn(administrativeInformation);
    resourceParts.add(es);
    resourceParts.add(property);

    final Map<JPAExpandItem, JPAAssociationPath> act = Utility.determineAssociations(helper.sd, resourceParts,
        expandOption);

    assertEquals(2, act.size());
  }

  @Test
  void testDetermineAssociationsSelectOneProperty() throws ODataException {
    final TestHelper helper = getHelper();
    final JPAPath commentPath = mock(JPAPath.class);
    final Set<JPAPath> selectOptions = new HashSet<>();
    final UriResourceEntitySet es = createEntitySetResource("Organizations");
    final JPACollectionAttribute pathLeaf = mock(JPACollectionAttribute.class);
    final EdmEntityType edmType = mock(EdmEntityType.class);

    when(commentPath.getLeaf()).thenReturn(pathLeaf);
    when(commentPath.getPath()).thenReturn(Collections.singletonList(pathLeaf));
    when(commentPath.getAlias()).thenReturn("Comment");
    when(es.getType()).thenReturn(edmType);
    when(edmType.getNamespace()).thenReturn("com.sap.olingo.jpa");
    when(edmType.getName()).thenReturn("Organization");

    resourceParts.add(es);
    selectOptions.add(commentPath);

    final Map<JPAPath, JPAAssociationPath> act = Utility.determineAssociations(helper.sd, resourceParts,
        selectOptions);

    assertEquals(1, act.size());
    assertNotNull(act.get(commentPath));
  }

  @Test
  void testDetermineAssociationsUriPathSelectPath() throws ODataException {
    final TestHelper helper = getHelper();
    final Set<JPAPath> selectOptions = new HashSet<>();
    final UriResourceEntitySet es = createEntitySetResource("CollectionDeeps");
    final UriResourceComplexProperty cp = createComplexPropertyResource("FirstLevel");
    final EdmEntityType edmType = mock(EdmEntityType.class);

    final JPAAttribute pathParent = mock(JPAAttribute.class);
    final JPAPath commentPath = mock(JPAPath.class);
    final JPACollectionAttribute pathLeaf = mock(JPACollectionAttribute.class);

    when(commentPath.getLeaf()).thenReturn(pathLeaf);
    when(commentPath.getPath()).thenReturn(Arrays.asList(pathParent, pathLeaf));
    when(commentPath.getAlias()).thenReturn("SecondLevel/Comment");
    when(es.getType()).thenReturn(edmType);
    when(edmType.getNamespace()).thenReturn("com.sap.olingo.jpa");
    when(edmType.getName()).thenReturn("CollectionDeep");

    resourceParts.add(es);
    resourceParts.add(cp);
    selectOptions.add(commentPath);

    final Map<JPAPath, JPAAssociationPath> act = Utility.determineAssociations(helper.sd, resourceParts,
        selectOptions);

    assertEquals(1, act.size());
    assertNotNull(act.get(commentPath));
    assertEquals("FirstLevel/SecondLevel/Comment", act.get(commentPath).getAlias());
  }

  private UriResourceNavigation createNavigationResource() {
    final EdmNavigationProperty property = mock(EdmNavigationProperty.class);
    final UriResourceNavigation navigation = mock(UriResourceNavigation.class);
    when(navigation.getKind()).thenReturn(UriResourceKind.navigationProperty);
    when(navigation.getProperty()).thenReturn(property);
    when(property.getName()).thenReturn("Navigation");
    when(navigation.getKeyPredicates()).thenReturn(Collections.emptyList());
    return navigation;
  }

  private UriResourceEntitySet createEntitySetResource(final String name) {
    return createEntitySetResource(createEntitySet(name, null));
  }

  private UriResourceEntitySet createEntitySetResource(final EdmEntitySet es) {
    final UriResourceEntitySet resourceItem = mock(UriResourceEntitySet.class);
    when(resourceItem.getKind()).thenReturn(UriResourceKind.entitySet);
    when(resourceItem.getEntitySet()).thenReturn(es);
    when(resourceItem.getKeyPredicates()).thenReturn(Collections.emptyList());
    return resourceItem;
  }

  private UriResourceEntitySet createEntitySetResource(final String string, final EdmEntityType person) {
    // TODO Auto-generated method stub
    return null;
  }

  private UriResourceSingleton createSingletonResource(final EdmSingleton es) {
    final UriResourceSingleton resourceItem = mock(UriResourceSingleton.class);
    when(resourceItem.getKind()).thenReturn(UriResourceKind.singleton);
    when(resourceItem.getSingleton()).thenReturn(es);
    return resourceItem;
  }

  private UriResourceComplexProperty createComplexPropertyResource(final String name) {
    final EdmProperty property = mock(EdmProperty.class);
    final EdmComplexType complexType = mock(EdmComplexType.class);
    when(property.getName()).thenReturn(name);
    when(complexType.getNamespace()).thenReturn(PUNIT_NAME);
    when(complexType.getName()).thenReturn(name);

    return createComplexPropertyResource(complexType, property);
  }

  private UriResourceComplexProperty createComplexPropertyResource(final EdmComplexType complexType,
      final EdmProperty property) {
    final UriResourceComplexProperty resourceItem = mock(UriResourceComplexProperty.class);

    when(resourceItem.getKind()).thenReturn(UriResourceKind.complexProperty);
    when(resourceItem.getProperty()).thenReturn(property);
    when(resourceItem.getComplexType()).thenReturn(complexType);
    return resourceItem;
  }

  private EdmNavigationProperty createNavigationProperty(final String name) {
    final var entityType = mock(EdmEntityType.class);
    final var keyProperty = mock(EdmKeyPropertyRef.class);
    final var navigationProperty = mock(EdmNavigationProperty.class);
    when(navigationProperty.getName()).thenReturn(name);
    when(navigationProperty.getType()).thenReturn(entityType);
    when(entityType.getKeyPropertyRefs()).thenReturn(Collections.singletonList(keyProperty));
    return navigationProperty;
  }

  private EdmComplexType createComplexType(final String name, final String navigationName,
      final EdmNavigationProperty navigationProperty) {
    final var complexType = mock(EdmComplexType.class);
    when(complexType.getName()).thenReturn(name);
    when(complexType.getNavigationProperty(navigationName)).thenReturn(navigationProperty);
    return complexType;
  }

  private EdmComplexType createComplexType(final String name, final Property... properties) {
    final var complexType = mock(EdmComplexType.class);
    when(complexType.getName()).thenReturn(name);
    for (final var property : properties) {
      final var edmProperty = mock(EdmElement.class);
      when(complexType.getProperty(property.name)).thenReturn(edmProperty);
      when(edmProperty.getType()).thenReturn(property.type);
    }
    return complexType;
  }

  private EdmEntityType createEntityType(final String name, final Property... properties) {
    final var entityType = mock(EdmEntityType.class);
    when(entityType.getName()).thenReturn(name);
    for (final var property : properties) {
      final var edmProperty = mock(EdmElement.class);
      when(entityType.getProperty(property.name)).thenReturn(edmProperty);
      when(edmProperty.getType()).thenReturn(property.type);
    }
    return entityType;
  }

  private EdmEntitySet createEntitySet(final String name, final EdmEntityType et) {
    final EdmEntitySet es = mock(EdmEntitySet.class);
    when(es.getName()).thenReturn(name);
    when(es.getEntityType()).thenReturn(et);
    return es;
  }

  private static record Property(String name, EdmType type) {

  }
}
