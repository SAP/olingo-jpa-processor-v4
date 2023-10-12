package com.sap.olingo.jpa.processor.core.modify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.converter.JPACollectionResult;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.processor.JPAODataInternalRequestContext;
import com.sap.olingo.jpa.processor.core.util.ServiceMetadataDouble;
import com.sap.olingo.jpa.processor.core.util.TestBase;

public abstract class TestJPACreateResult extends TestBase {

  protected JPAExpandResult cut;
  protected JPAEntityType et;
  protected Object jpaEntity;
  protected JPATupleChildConverter converter;
  protected JPAODataRequestContextAccess requestContext;
  protected JPAODataRequestContext context;
  protected JPAODataSessionContextAccess sessionContext;

  public TestJPACreateResult() {
    super();
    context = mock(JPAODataRequestContext.class);
    sessionContext = mock(JPAODataSessionContextAccess.class);
    requestContext = new JPAODataInternalRequestContext(context, sessionContext);
  }

  @Test
  public void testGetChildrenProvidesEmptyMap() throws ODataJPAModelException, ODataApplicationException {
    converter = new JPATupleChildConverter(helper.sd, OData.newInstance()
        .createUriHelper(), new ServiceMetadataDouble(nameBuilder, "Organizations"), requestContext);

    createCutProvidesEmptyMap();

    final Map<JPAAssociationPath, JPAExpandResult> act = cut.getChildren();

    assertNotNull(act);
    assertEquals(1, act.size());
  }

  @Test
  public void testGetResultSimpleEntity() throws ODataJPAModelException, ODataApplicationException {
    et = helper.getJPAEntityType("BusinessPartnerRoles");

    createCutGetResultSimpleEntity();

    final List<Tuple> act = cut.getResult("root");

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("34", act.get(0).get("BusinessPartnerID"));
  }

  @Test
  public void testGetResultEntityWithTransient() throws ODataJPAModelException, ODataApplicationException {
    et = helper.getJPAEntityType("Persons");

    createCutGetResultEntityWithTransient();

    final List<Tuple> act = cut.getResult("root");

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("Hubert, Hans", act.get(0).get("FullName"));
  }

  @Test
  public void testGetResultWithOneLevelEmbedded() throws ODataJPAModelException, ODataApplicationException {
    et = helper.getJPAEntityType("AdministrativeDivisionDescriptions");

    createCutGetResultWithOneLevelEmbedded();

    final List<Tuple> act = cut.getResult("root");

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("A", act.get(0).get("CodeID"));
    assertEquals("Hugo", act.get(0).get("Name"));
  }

  @Test
  public void testGetResultWithTwoLevelEmbedded() throws ODataJPAModelException, ODataApplicationException {

    createCutGetResultWithTwoLevelEmbedded();

    final List<Tuple> act = cut.getResult("root");
    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("01", act.get(0).get("ID"));
    assertEquals("99", act.get(0).get("AdministrativeInformation/Created/By"));
  }

  @Test
  public void testGetResultWithOneLinked() throws ODataJPAModelException, ODataApplicationException {
    createCutGetResultWithWithOneLinked();
    final Map<JPAAssociationPath, JPAExpandResult> act = cut.getChildren();
    assertNotNull(act);
    assertEquals(1, act.size());
    for (final JPAAssociationPath actPath : act.keySet()) {
      assertEquals("Children", actPath.getAlias());
      final List<Tuple> subResult = act.get(actPath).getResult("Eurostat/NUTS1/BE2");
      assertEquals(1, subResult.size());
    }
  }

  @Test
  public void testGetResultWithDescriptionProperty() throws ODataJPAModelException, ODataApplicationException {

    createCutGetResultWithDescriptionProperty();
    final List<Tuple> act = cut.getResult("root");
    assertEquals(1, act.size());
    final Tuple actResult = act.get(0);
    assertEquals(7L, actResult.get("ETag"));
    assertEquals("MyDivision", actResult.get("LocationName"));
  }

  @Test
  public void testGetResultWithTwoLinked() throws ODataJPAModelException, ODataApplicationException {
    createCutGetResultWithWithTwoLinked();
    final Map<JPAAssociationPath, JPAExpandResult> act = cut.getChildren();
    assertNotNull(act);
    assertEquals(1, act.size());
    for (final JPAAssociationPath actPath : act.keySet()) {
      assertEquals("Children", actPath.getAlias());
      final List<Tuple> subResult = act.get(actPath).getResult("Eurostat/NUTS1/BE2");
      assertEquals(2, subResult.size());
    }
  }

  @Test
  public void testGetResultWithPrimitiveCollection() throws ODataJPAModelException, ODataApplicationException {
    createCutGetResultEntityWithSimpleCollection();

    final Map<JPAAssociationPath, JPAExpandResult> act = cut.getChildren();
    assertDoesNotContain(cut.getResult("root"), "Comment");
    assertNotNull(act);
    assertFalse(act.isEmpty());
    for (final Entry<JPAAssociationPath, JPAExpandResult> entity : act.entrySet()) {
      assertEquals(1, entity.getValue().getResults().size());
      assertEquals("Comment", entity.getKey().getAlias());
      final Collection<Object> actConverted = ((JPACollectionResult) entity.getValue()).getPropertyCollection(
          JPAExpandResult.ROOT_RESULT_KEY);
      assertEquals(2, actConverted.size());
      for (final Object o : actConverted) {
        assertNotNull(o);
        assertFalse(((String) o).isEmpty());
      }
    }
  }

  @Test
  public void testGetResultWithComplexCollection() throws ODataJPAModelException, ODataApplicationException {
    createCutGetResultEntityWithComplexCollection();

    final Map<JPAAssociationPath, JPAExpandResult> act = cut.getChildren();
    assertDoesNotContain(cut.getResult("root"), "InhouseAddress");
    assertNotNull(act);
    assertFalse(act.isEmpty());
    for (final Entry<JPAAssociationPath, JPAExpandResult> entity : act.entrySet()) {
      assertEquals(1, entity.getValue().getResults().size());
      assertEquals("InhouseAddress", entity.getKey().getAlias());
      final Collection<Object> actConverted = ((JPACollectionResult) entity.getValue()).getPropertyCollection(
          JPAExpandResult.ROOT_RESULT_KEY);
      assertEquals(2, actConverted.size());
      for (final Object o : actConverted) {
        assertNotNull(o);
        assertFalse(((ComplexValue) o).getValue().isEmpty());
      }
    }
  }

  @Test
  public void testGetResultWithComplexContainingCollection() throws ODataJPAModelException, ODataApplicationException {
    createCutGetResultEntityWithComplexWithCollection();

    final Map<JPAAssociationPath, JPAExpandResult> act = cut.getChildren();
    boolean found = false;
    assertDoesNotContain(cut.getResult("root"), "Complex/Address");
    assertNotNull(act);
    assertFalse(act.isEmpty());
    for (final Entry<JPAAssociationPath, JPAExpandResult> entity : act.entrySet()) {
      if (entity.getKey().getAlias().equals("Complex/Address")) {
        found = true;
        assertEquals(1, entity.getValue().getResults().size());
        assertEquals("Complex/Address", entity.getKey().getAlias());
        final Collection<Object> actConverted = ((JPACollectionResult) entity.getValue()).getPropertyCollection(
            JPAExpandResult.ROOT_RESULT_KEY);
        assertEquals(2, actConverted.size());
        for (final Object o : actConverted) {
          assertNotNull(o);
          assertFalse(((ComplexValue) o).getValue().isEmpty());
        }
      }
    }
    assertTrue(found);
  }

  @Test
  public void testGetResultWithContainingNestedComplexCollection() throws ODataJPAModelException,
      ODataApplicationException {
    createCutGetResultEntityWithNestedComplexCollection();

    final Map<JPAAssociationPath, JPAExpandResult> act = cut.getChildren();
    boolean found = false;
    assertDoesNotContain(cut.getResult("root"), "Nested");
    assertNotNull(act);
    assertFalse(act.isEmpty());
    for (final Entry<JPAAssociationPath, JPAExpandResult> entity : act.entrySet()) {
      if (entity.getKey().getAlias().equals("Nested")) {
        found = true;
        assertEquals(1, entity.getValue().getResults().size());
        assertEquals("Nested", entity.getKey().getAlias());
        final Collection<Object> actConverted = ((JPACollectionResult) entity.getValue()).getPropertyCollection(
            JPAExpandResult.ROOT_RESULT_KEY);
        assertEquals(2, actConverted.size());
        for (final Object o : actConverted) {
          assertNotNull(o);
          assertFalse(((ComplexValue) o).getValue().isEmpty());
        }
      }
    }
    assertTrue(found);
  }

  @Test
  public void testGetResultWithDeepComplexContainingCollection() throws ODataJPAModelException,
      ODataApplicationException {
    createCutGetResultEntityWithDeepComplexWithCollection();

    final Map<JPAAssociationPath, JPAExpandResult> act = cut.getChildren();
    boolean found = false;
    assertDoesNotContain(cut.getResult("root"), "FirstLevel/SecondLevel/Address");
    assertNotNull(act);
    assertFalse(act.isEmpty());
    for (final Entry<JPAAssociationPath, JPAExpandResult> entity : act.entrySet()) {
      if (entity.getKey().getAlias().equals("FirstLevel/SecondLevel/Address")) {
        found = true;
        assertEquals(1, entity.getValue().getResults().size());
        assertEquals("FirstLevel/SecondLevel/Address", entity.getKey().getAlias());
        final Collection<Object> actConverted = ((JPACollectionResult) entity.getValue()).getPropertyCollection(
            JPAExpandResult.ROOT_RESULT_KEY);
        assertEquals(2, actConverted.size());
        for (final Object o : actConverted) {
          assertNotNull(o);
          assertFalse(((ComplexValue) o).getValue().isEmpty());
        }
      }
    }
    assertTrue(found);
  }

  @Test
  public void testHasCountReturns() throws ODataJPAModelException, ODataApplicationException {
    createCutProvidesEmptyMap();
    assertFalse(cut.hasCount());
  }

  private void assertDoesNotContain(final List<Tuple> result, final String prefix) {
    for (final Tuple t : result) {
      for (final TupleElement<?> e : t.getElements())
        assertFalse(e.getAlias().startsWith(prefix), e.getAlias() + " violates prefix check: " + prefix);
    }

  }

  protected abstract void createCutProvidesEmptyMap() throws ODataJPAModelException, ODataApplicationException;

  protected abstract void createCutGetResultEntityWithDeepComplexWithCollection() throws ODataJPAModelException,
      ODataApplicationException;

  protected abstract void createCutGetResultEntityWithNestedComplexCollection() throws ODataJPAModelException,
      ODataApplicationException;

  protected abstract void createCutGetResultEntityWithComplexCollection() throws ODataJPAModelException,
      ODataApplicationException;

  protected abstract void createCutGetResultWithWithTwoLinked() throws ODataJPAModelException,
      ODataApplicationException;

  protected abstract void createCutGetResultWithWithOneLinked() throws ODataJPAModelException,
      ODataApplicationException;

  protected abstract void createCutGetResultWithDescriptionProperty() throws ODataJPAModelException,
      ODataApplicationException;

  protected abstract void createCutGetResultSimpleEntity() throws ODataJPAModelException, ODataApplicationException;

  protected abstract void createCutGetResultWithOneLevelEmbedded() throws ODataJPAModelException,
      ODataApplicationException;

  protected abstract void createCutGetResultWithTwoLevelEmbedded() throws ODataJPAModelException,
      ODataApplicationException;

  protected abstract void createCutGetResultEntityWithSimpleCollection() throws ODataJPAModelException,
      ODataApplicationException;

  protected abstract void createCutGetResultEntityWithComplexWithCollection() throws ODataJPAModelException,
      ODataApplicationException;

  protected abstract void createCutGetResultEntityWithTransient() throws ODataJPAModelException,
      ODataApplicationException;
}