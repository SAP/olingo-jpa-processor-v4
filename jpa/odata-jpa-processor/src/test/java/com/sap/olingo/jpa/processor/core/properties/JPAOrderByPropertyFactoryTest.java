package com.sap.olingo.jpa.processor.core.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceCount;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalArgumentException;
import com.sap.olingo.jpa.processor.core.testmodel.AssociationOneToOneSource;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionDeep;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

class JPAOrderByPropertyFactoryTest extends TestBase {
  private JPAOrderByPropertyFactory cut;
  private TestHelper testHelper;
  private OrderByItem orderByItem;
  private JPAEntityType et;
  private Member expression;
  private UriInfoResource uriInfo;

  @BeforeEach
  void setup() throws ODataException {
    orderByItem = mock(OrderByItem.class);
    et = mock(JPAEntityType.class);
    expression = mock(Member.class);
    uriInfo = mock(UriInfoResource.class);
    when(orderByItem.getExpression()).thenReturn(expression);
    when(expression.getResourcePath()).thenReturn(uriInfo);
    cut = new JPAOrderByPropertyFactory();
    testHelper = getHelper();
  }

  @Test
  void testCreatePrimitiveSimpleProperty() throws ODataJPAModelException {
    final var property = mock(UriResourcePrimitiveProperty.class);
    final var edmType = mock(EdmProperty.class);
    when(uriInfo.getUriResourceParts()).thenReturn(Collections.singletonList(property));
    when(property.getProperty()).thenReturn(edmType);
    when(property.getKind()).thenReturn(UriResourceKind.primitiveProperty);
    when(edmType.getName()).thenReturn("Name1");

    et = testHelper.getJPAEntityType(Organization.class);

    final var act = cut.createProperty(orderByItem, et, Locale.ENGLISH);
    assertTrue(act instanceof JPAProcessorSimpleAttribute);
    assertTrue(((JPAProcessorSimpleAttribute) act).isSortable());
    assertFalse(((JPAProcessorSimpleAttribute) act).requiresJoin());
    assertEquals("Name1", act.getAlias());
  }

  @Test
  void testCreateComplexSimpleProperty() throws ODataJPAModelException {
    final var complex = mock(UriResourceComplexProperty.class);
    final var property = mock(UriResourcePrimitiveProperty.class);
    final var edmProperty = mock(EdmProperty.class);
    final var edmComplex = mock(EdmProperty.class);
    when(uriInfo.getUriResourceParts()).thenReturn(Arrays.asList(complex, property));
    when(property.getProperty()).thenReturn(edmProperty);
    when(edmProperty.getName()).thenReturn("Region");
    when(property.getKind()).thenReturn(UriResourceKind.primitiveProperty);
    when(complex.getProperty()).thenReturn(edmComplex);
    when(edmComplex.getName()).thenReturn("Address");

    et = testHelper.getJPAEntityType(Organization.class);

    final var act = cut.createProperty(orderByItem, et, Locale.ENGLISH);
    assertTrue(act instanceof JPAProcessorSimpleAttribute);
    assertTrue(((JPAProcessorSimpleAttribute) act).isSortable());
    assertFalse(((JPAProcessorSimpleAttribute) act).requiresJoin());
    assertEquals("Address/Region", act.getAlias());
  }

  @Test
  void testCreateNavigationSimpleProperty() throws ODataJPAModelException {
    // "AssociationOneToOneSources?$orderby=ColumnTarget/Source asc"
    final var navigation = mock(UriResourceNavigation.class);
    final var property = mock(UriResourcePrimitiveProperty.class);
    final var edmProperty = mock(EdmProperty.class);
    final var edmNavigation = mock(EdmNavigationProperty.class);
    when(uriInfo.getUriResourceParts()).thenReturn(Arrays.asList(navigation, property));
    when(property.getProperty()).thenReturn(edmProperty);
    when(edmProperty.getName()).thenReturn("Source");
    when(property.getKind()).thenReturn(UriResourceKind.primitiveProperty);
    when(navigation.getProperty()).thenReturn(edmNavigation);
    when(edmNavigation.getName()).thenReturn("ColumnTarget");
    when(orderByItem.isDescending()).thenReturn(false);

    et = testHelper.getJPAEntityType(AssociationOneToOneSource.class);

    final var act = cut.createProperty(orderByItem, et, Locale.ENGLISH);
    assertTrue(act instanceof JPAProcessorSimpleAttribute);
    assertTrue(((JPAProcessorSimpleAttribute) act).isSortable());
    assertTrue(((JPAProcessorSimpleAttribute) act).requiresJoin());
    assertEquals("ColumnTarget", act.getAlias());
  }

  @Test
  void testCreateNavigationComplexPathProperty() throws ODataJPAModelException {
    // "Persons?$orderby=AdministrativeInformation/Created/User/LastName"
    final var firstComplex = mock(UriResourceComplexProperty.class);
    final var secondComplex = mock(UriResourceComplexProperty.class);
    final var edmFirstProperty = mock(EdmProperty.class);
    final var edmSecondProperty = mock(EdmProperty.class);

    final var navigation = mock(UriResourceNavigation.class);
    final var property = mock(UriResourcePrimitiveProperty.class);
    final var edmProperty = mock(EdmProperty.class);
    final var edmNavigation = mock(EdmNavigationProperty.class);

    when(uriInfo.getUriResourceParts()).thenReturn(Arrays.asList(firstComplex, secondComplex, navigation, property));

    when(firstComplex.getProperty()).thenReturn(edmFirstProperty);
    when(edmFirstProperty.getName()).thenReturn("AdministrativeInformation");
    when(secondComplex.getProperty()).thenReturn(edmSecondProperty);
    when(edmSecondProperty.getName()).thenReturn("Created");

    when(navigation.getProperty()).thenReturn(edmNavigation);
    when(edmNavigation.getName()).thenReturn("User");
    when(property.getProperty()).thenReturn(edmProperty);
    when(edmProperty.getName()).thenReturn("LastName");
    when(property.getKind()).thenReturn(UriResourceKind.primitiveProperty);
    when(orderByItem.isDescending()).thenReturn(false);

    et = testHelper.getJPAEntityType(Person.class);

    final var act = cut.createProperty(orderByItem, et, Locale.ENGLISH);
    assertTrue(act instanceof JPAProcessorSimpleAttribute);
    assertTrue(((JPAProcessorSimpleAttribute) act).isSortable());
    assertTrue(((JPAProcessorSimpleAttribute) act).requiresJoin());
    assertEquals("AdministrativeInformation/Created/User", act.getAlias());
  }

  @Test
  void testCreateNavigationCount() throws ODataJPAModelException {
    // "Organizations?$orderby=Roles/$count"
    final var navigation = mock(UriResourceNavigation.class);
    final var count = mock(UriResourceCount.class);
    final var edmNavigation = mock(EdmNavigationProperty.class);
    when(uriInfo.getUriResourceParts()).thenReturn(Arrays.asList(navigation, count));
    when(navigation.getProperty()).thenReturn(edmNavigation);
    when(edmNavigation.getName()).thenReturn("Roles");
    when(count.getKind()).thenReturn(UriResourceKind.count);
    when(orderByItem.isDescending()).thenReturn(false);

    et = testHelper.getJPAEntityType(Organization.class);

    final var act = cut.createProperty(orderByItem, et, Locale.ENGLISH);
    assertTrue(act instanceof JPAProcessorCountAttribute);
    assertTrue(((JPAProcessorCountAttribute) act).isSortable());
    assertTrue(((JPAProcessorCountAttribute) act).requiresJoin());
    assertEquals("Roles", act.getAlias());
  }

  @Test
  void testCreatePrimitiveCollectionCountViaComplex() throws ODataJPAModelException {
    // "CollectionDeeps?$orderby=FirstLevel/SecondLevel/Comment/$count asc"
    final var firstComplex = mock(UriResourceComplexProperty.class);
    final var secondComplex = mock(UriResourceComplexProperty.class);
    final var edmFirstProperty = mock(EdmProperty.class);
    final var edmSecondProperty = mock(EdmProperty.class);

    when(firstComplex.getProperty()).thenReturn(edmFirstProperty);
    when(edmFirstProperty.getName()).thenReturn("FirstLevel");
    when(secondComplex.getProperty()).thenReturn(edmSecondProperty);
    when(edmSecondProperty.getName()).thenReturn("SecondLevel");

    final var collection = mock(UriResourcePrimitiveProperty.class);
    final var edmCollection = mock(EdmProperty.class);

    final var count = mock(UriResourceCount.class);
    when(uriInfo.getUriResourceParts()).thenReturn(Arrays.asList(firstComplex, secondComplex, collection, count));

    when(collection.getProperty()).thenReturn(edmCollection);
    when(collection.isCollection()).thenReturn(true);
    when(edmCollection.getName()).thenReturn("Comment");
    when(count.getKind()).thenReturn(UriResourceKind.count);
    when(orderByItem.isDescending()).thenReturn(false);

    et = testHelper.getJPAEntityType(CollectionDeep.class);

    final var act = cut.createProperty(orderByItem, et, Locale.ENGLISH);
    assertTrue(act instanceof JPAProcessorCountAttribute);
    assertTrue(((JPAProcessorCountAttribute) act).isSortable());
    assertTrue(((JPAProcessorCountAttribute) act).requiresJoin());
    assertEquals("FirstLevel/SecondLevel/Comment", act.getAlias());
  }

  @Test
  void testCreateComplexCollectionCountViaComplex() throws ODataJPAModelException {
    // "CollectionDeeps?$orderby=FirstLevel/SecondLevel/Address/$count asc"
    final var firstComplex = mock(UriResourceComplexProperty.class);
    final var secondComplex = mock(UriResourceComplexProperty.class);
    final var edmFirstProperty = mock(EdmProperty.class);
    final var edmSecondProperty = mock(EdmProperty.class);

    when(firstComplex.getProperty()).thenReturn(edmFirstProperty);
    when(edmFirstProperty.getName()).thenReturn("FirstLevel");
    when(secondComplex.getProperty()).thenReturn(edmSecondProperty);
    when(edmSecondProperty.getName()).thenReturn("SecondLevel");

    final var collection = mock(UriResourceComplexProperty.class);
    final var edmCollection = mock(EdmProperty.class);

    final var count = mock(UriResourceCount.class);
    when(uriInfo.getUriResourceParts()).thenReturn(Arrays.asList(firstComplex, secondComplex, collection, count));

    when(collection.getProperty()).thenReturn(edmCollection);
    when(collection.isCollection()).thenReturn(true);
    when(edmCollection.getName()).thenReturn("Address");
    when(count.getKind()).thenReturn(UriResourceKind.count);
    when(orderByItem.isDescending()).thenReturn(false);

    et = testHelper.getJPAEntityType(CollectionDeep.class);

    final var act = cut.createProperty(orderByItem, et, Locale.ENGLISH);
    assertTrue(act instanceof JPAProcessorCountAttribute);
    assertTrue(((JPAProcessorCountAttribute) act).isSortable());
    assertTrue(((JPAProcessorCountAttribute) act).requiresJoin());
    assertEquals("FirstLevel/SecondLevel/Address", act.getAlias());
  }

  @Test
  void testCreateDescriptionSimpleProperty() throws ODataJPAModelException {
    final var property = mock(UriResourcePrimitiveProperty.class);
    final var edmType = mock(EdmProperty.class);
    when(uriInfo.getUriResourceParts()).thenReturn(Collections.singletonList(property));
    when(property.getProperty()).thenReturn(edmType);
    when(property.getKind()).thenReturn(UriResourceKind.primitiveProperty);
    when(edmType.getName()).thenReturn("LocationName");

    et = testHelper.getJPAEntityType(Organization.class);

    final var act = cut.createProperty(orderByItem, et, Locale.ENGLISH);
    assertTrue(act instanceof JPAProcessorDescriptionAttribute);
    assertTrue(((JPAProcessorDescriptionAttribute) act).isSortable());
    assertTrue(((JPAProcessorDescriptionAttribute) act).requiresJoin());
    assertEquals("LocationName", act.getAlias());
  }

  @Test
  void testThrowsBadRequestExceptionOnUnknownProperty() throws ODataJPAModelException {
    final var property = mock(UriResourcePrimitiveProperty.class);
    final var edmType = mock(EdmProperty.class);
    when(uriInfo.getUriResourceParts()).thenReturn(Collections.singletonList(property));
    when(property.getProperty()).thenReturn(edmType);
    when(property.getKind()).thenReturn(UriResourceKind.primitiveProperty);
    when(edmType.getName()).thenReturn("Name");

    et = testHelper.getJPAEntityType(Organization.class);

    assertThrows(ODataJPAIllegalArgumentException.class,
        () -> cut.createProperty(orderByItem, et, Locale.ENGLISH));
  }

  @Test
  void testThrowsNotImplementedOnOrderByFunction() {
    final var function = mock(UriResourceFunction.class);
    final var edmType = mock(EdmFunction.class);
    when(uriInfo.getUriResourceParts()).thenReturn(Collections.singletonList(function));
    when(function.getFunction()).thenReturn(edmType);
    when(function.getKind()).thenReturn(UriResourceKind.function);
    when(edmType.getName()).thenReturn("Name");

    assertThrows(ODataJPAIllegalArgumentException.class,
        () -> cut.createProperty(orderByItem, et, Locale.ENGLISH));
  }

}
