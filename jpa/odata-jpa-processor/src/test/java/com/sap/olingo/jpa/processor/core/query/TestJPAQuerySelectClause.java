package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.testmodel.TestDataConstants.NO_ATTRIBUTES_BUSINESS_PARTNER_T;
import static com.sap.olingo.jpa.processor.core.testmodel.TestDataConstants.NO_ATTRIBUTES_POSTAL_ADDRESS;
import static com.sap.olingo.jpa.processor.core.testmodel.TestDataConstants.NO_ATTRIBUTES_POSTAL_ADDRESS_T;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.Selection;

import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceValue;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.util.EdmEntityTypeDouble;
import com.sap.olingo.jpa.processor.core.util.EdmPropertyDouble;
import com.sap.olingo.jpa.processor.core.util.ExpandItemDouble;
import com.sap.olingo.jpa.processor.core.util.ExpandOptionDouble;
import com.sap.olingo.jpa.processor.core.util.SelectOptionDouble;
import com.sap.olingo.jpa.processor.core.util.TestQueryBase;
import com.sap.olingo.jpa.processor.core.util.UriInfoDouble;
import com.sap.olingo.jpa.processor.core.util.UriResourceNavigationDouble;
import com.sap.olingo.jpa.processor.core.util.UriResourcePropertyDouble;

class TestJPAQuerySelectClause extends TestQueryBase {

  @Test
  void checkSelectAll() throws ODataApplicationException, ODataJPAModelException {
    fillJoinTable(root);

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("*"))).joinedPersistent(), root, Collections.emptyList());
    assertEquals(jpaEntityType.getPathList().size() - NO_ATTRIBUTES_BUSINESS_PARTNER_T, selectClause.size());
  }

  @Test
  void checkSelectAllWithSelectionNull() throws ODataApplicationException, ODataJPAModelException {
    fillJoinTable(root);
    final SelectOption selOpts = null;
    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(selOpts)).joinedPersistent(), root, Collections.emptyList());

    assertEquals(jpaEntityType.getPathList().size() - NO_ATTRIBUTES_BUSINESS_PARTNER_T, selectClause.size());
  }

  @Test
  void checkSelectExpandViaIgnoredProperties() throws ODataApplicationException, ODataJPAModelException {
    // Organizations('3')/Address?$expand=AdministrativeDivision
    fillJoinTable(root);
    final List<ExpandItem> expItems = new ArrayList<>();
    final EdmEntityType startEntity = new EdmEntityTypeDouble(nameBuilder, "Organization");
    final EdmEntityType targetEntity = new EdmEntityTypeDouble(nameBuilder, "AdministrativeDivision");
    final SelectOption selOpts = null;

    final ExpandOption expOps = new ExpandOptionDouble("AdministrativeDivision", expItems);
    expItems.add(new ExpandItemDouble(targetEntity));
    final List<UriResource> startResources = new ArrayList<>();
    final UriInfoDouble uriInfo = new UriInfoDouble(selOpts);
    uriInfo.setExpandOpts(expOps);
    uriInfo.setUriResources(startResources);

    startResources.add(new UriResourceNavigationDouble(startEntity));
    startResources.add(new UriResourcePropertyDouble(new EdmPropertyDouble("Address")));

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(uriInfo)
        .joinedPersistent(), root, Collections.emptyList());

    assertContains(selectClause, "Address/RegionCodeID");
  }

  @Test
  void checkSelectOnePropertyCreatedAt() throws ODataApplicationException, ODataJPAModelException {
    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("CreationDateTime"))).joinedPersistent(), root, Collections
            .emptyList());
    assertEquals(3, selectClause.size());
    assertContains(selectClause, "CreationDateTime");
    assertContains(selectClause, "ID");
    assertContains(selectClause, "ETag");
  }

  @Test
  void checkSelectOnePropertyID() throws ODataApplicationException, ODataJPAModelException {
    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("ID"))).joinedPersistent(), root, Collections.emptyList());
    assertEquals(2, selectClause.size());
    assertContains(selectClause, "ID");
    assertContains(selectClause, "ETag");
  }

  @Test
  void checkSelectOnePropertyPartKey() throws ODataException, ODataJPAIllegalAccessException {
    jpaEntityType = helper.getJPAEntityType("AdministrativeDivisionDescriptions");
    buildRequestContext("AdministrativeDivisionDescriptions", "AdministrativeDivisionDescription");

    cut = new JPAJoinQuery(null, requestContext);

    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble((new SelectOptionDouble("CodePublisher")))).joinedPersistent(), root, Collections
            .emptyList());
    assertEquals(4, selectClause.size());
    assertContains(selectClause, "CodePublisher");
    assertContains(selectClause, "CodeID");
    assertContains(selectClause, "DivisionCode");
    assertContains(selectClause, "Language");
  }

  @Test
  void checkSelectPropertyTypeCreatedAt() throws ODataApplicationException, ODataJPAModelException {
    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Type,CreationDateTime"))).joinedPersistent(), root, Collections
            .emptyList());

    assertEquals(4, selectClause.size());
    assertContains(selectClause, "CreationDateTime");
    assertContains(selectClause, "Type");
    assertContains(selectClause, "ETag");
    assertContains(selectClause, "ID");
  }

  @Test
  void checkSelectSupertypePropertyTypeName2() throws ODataException, ODataJPAIllegalAccessException {
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);
    buildRequestContext("Organizations", "Organization");

    cut = new JPAJoinQuery(null, requestContext);

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Type,Name2"))).joinedPersistent(), root, Collections.emptyList());
    assertContains(selectClause, "Name2");
    assertContains(selectClause, "Type");
    assertContains(selectClause, "ID");
    assertEquals(3, selectClause.size());
  }

  @Test
  void checkSelectCompleteComplexType() throws ODataException {
    // Organizations$select=Address
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);
    fillJoinTable(root);

    cut = new JPAJoinQuery(null, requestContext);

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Address"))).joinedPersistent(), root, Collections.emptyList());
    assertEquals(NO_ATTRIBUTES_POSTAL_ADDRESS + 2 - NO_ATTRIBUTES_POSTAL_ADDRESS_T, selectClause.size());
  }

  @Test
  void checkSelectCompleteNestedComplexTypeLowLevel() throws ODataException {
    // Organizations$select=Address
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("AdministrativeInformation/Created"))).joinedPersistent(), root,
        Collections.emptyList());
    assertEquals(4, selectClause.size());
    assertContains(selectClause, "AdministrativeInformation/Created/By");
    assertContains(selectClause, "AdministrativeInformation/Created/At");
    assertContains(selectClause, "ETag");
    assertContains(selectClause, "ID");
  }

  @Test
  void checkSelectCompleteNestedComplexTypeHighLevel() throws ODataException {
    // Organizations$select=Address
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("AdministrativeInformation"))).joinedPersistent(), root, Collections
            .emptyList());
    assertEquals(6, selectClause.size());
    assertContains(selectClause, "AdministrativeInformation/Created/By");
    assertContains(selectClause, "AdministrativeInformation/Created/At");
    assertContains(selectClause, "AdministrativeInformation/Updated/By");
    assertContains(selectClause, "AdministrativeInformation/Updated/At");
    assertContains(selectClause, "ETag");
    assertContains(selectClause, "ID");
  }

  @Test
  void checkSelectElementOfComplexType() throws ODataException {
    // Organizations$select=Address/Country
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);

    // SELECT c.address.geocode FROM Company c WHERE c.name = 'Random House'
    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Address/Country"))).joinedPersistent(), root, Collections
            .emptyList());
    assertContains(selectClause, "Address/Country");
    assertContains(selectClause, "ID");
    assertContains(selectClause, "ETag");
    assertEquals(3, selectClause.size());
  }

  @Test
  void checkSelectCollectionProperty() throws ODataException, ODataJPAIllegalAccessException {
    // Organizations$select=Comment
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);
    fillJoinTable(root);
    buildUriInfo("Organizations", "Organization");
    requestContext.setUriInfo(uriInfo);
    cut = new JPAJoinQuery(null, requestContext);

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Comment"))).joinedPersistent(), root, Collections.emptyList());
    assertEquals(1, selectClause.size());
    assertEquals("ID", selectClause.get(0).getAlias());
  }

  @Test
  void checkSelectTextJoinSingleAttribute() throws ODataException {
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);
    fillJoinTable(root);

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Address/CountryName"))).joinedPersistent(), root, Collections
            .emptyList());
    assertContains(selectClause, "Address/CountryName");
    assertContains(selectClause, "ID");
    assertContains(selectClause, "ETag");
    assertEquals(3, selectClause.size());
  }

  @Test
  void checkSelectTextJoinComplexType() throws ODataException {
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);
    fillJoinTable(root);

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Address"))).joinedPersistent(), root, Collections.emptyList());
    assertEquals(NO_ATTRIBUTES_POSTAL_ADDRESS + 2 - NO_ATTRIBUTES_POSTAL_ADDRESS_T, selectClause
        .size());
    assertContains(selectClause, "Address/CountryName");
    assertContains(selectClause, "ETag");
    assertContains(selectClause, "ID");
  }

  @Test
  void checkSelectStreamValueStatic() throws ODataException, ODataJPAIllegalAccessException {
    jpaEntityType = helper.getJPAEntityType("PersonImages");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    buildRequestContext("PersonImages", "PersonImage");

    cut = new JPAJoinQuery(null, requestContext);

    final UriInfoDouble uriInfo = new UriInfoDouble(new SelectOptionDouble("Address"));
    final List<UriResource> uriResources = new ArrayList<>();
    uriInfo.setUriResources(uriResources);
    uriResources.add(new UriResourceEntitySetDouble());
    uriResources.add(new UriResourceValueDouble());

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(uriInfo)
        .joinedPersistent(), root, Collections.emptyList());
    assertNotNull(selectClause);
    assertContains(selectClause, "Image");
    assertContains(selectClause, "ID");
  }

  @Test
  void checkSelectStreamValueDynamic() throws ODataException, ODataJPAIllegalAccessException {
    jpaEntityType = helper.getJPAEntityType("OrganizationImages");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    buildRequestContext("OrganizationImages", "OrganizationImage");

    cut = new JPAJoinQuery(null, requestContext);

    final UriInfoDouble uriInfo = new UriInfoDouble(new SelectOptionDouble("Address"));
    final List<UriResource> uriResources = new ArrayList<>();
    uriInfo.setUriResources(uriResources);
    uriResources.add(new UriResourceEntitySetDouble());
    uriResources.add(new UriResourceValueDouble());

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(uriInfo)
        .joinedPersistent(), root, Collections.emptyList());
    assertNotNull(selectClause);
    assertContains(selectClause, "Image");
    assertContains(selectClause, "MimeType");
    assertContains(selectClause, "ID");
  }

  @Test
  void checkSelectPropertyValue() throws ODataException, ODataJPAIllegalAccessException {
    jpaEntityType = helper.getJPAEntityType("PersonImages");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    buildRequestContext("PersonImages", "PersonImage");
    cut = new JPAJoinQuery(null, requestContext);

    final SelectOption selOpts = null;
    final UriInfoDouble uriInfo = new UriInfoDouble(selOpts);
    final List<UriResource> uriResources = new ArrayList<>();
    uriInfo.setUriResources(uriResources);
    // PersonImages('99')/AdministrativeInformation/Created/By/$value
    uriResources.add(new UriResourceEntitySetDouble());
    uriResources.add(new UriResourceComplexPropertyDouble(new EdmPropertyDouble("AdministrativeInformation")));
    uriResources.add(new UriResourceComplexPropertyDouble(new EdmPropertyDouble("Created")));
    uriResources.add(new UriResourcePropertyDouble(new EdmPropertyDouble("By")));
    uriResources.add(new UriResourceValueDouble());

    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(uriInfo)
        .joinedPersistent(), root, Collections.emptyList());
    assertNotNull(selectClause);
    assertContains(selectClause, "AdministrativeInformation/Created/By");
    assertContains(selectClause, "ID");
  }

  @Test
  void checkSelectAllWithGroup() throws ODataApplicationException, ODataJPAModelException {

    fillJoinTable(root);
    final List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("*"))).joinedPersistent(), root, Collections.emptyList());
    assertEquals(jpaEntityType.getPathList().size() - NO_ATTRIBUTES_BUSINESS_PARTNER_T, selectClause.size());
  }

  private void assertContains(final List<Selection<?>> selectClause, final String alias) {
    for (final Selection<?> selection : selectClause) {
      if (selection.getAlias().equals(alias))
        return;
    }
    fail(alias + " not found");
  }

  private class UriResourceValueDouble implements UriResourceValue {

    @Override
    public UriResourceKind getKind() {
      return UriResourceKind.value;
    }

    @Override
    public String getSegmentValue() {
      return null;
    }
  }

  private class UriResourceComplexPropertyDouble implements UriResourceComplexProperty {
    private final EdmProperty property;

    public UriResourceComplexPropertyDouble(final EdmProperty property) {
      super();
      this.property = property;
    }

    @Override
    public EdmProperty getProperty() {
      return property;
    }

    @Override
    public EdmType getType() {
      fail();
      return null;
    }

    @Override
    public boolean isCollection() {
      fail();
      return false;
    }

    @Override
    public String getSegmentValue(final boolean includeFilters) {
      fail();
      return null;
    }

    @Override
    public String toString(final boolean includeFilters) {
      fail();
      return null;
    }

    @Override
    public UriResourceKind getKind() {
      fail();
      return null;
    }

    @Override
    public String getSegmentValue() {
      fail();
      return null;
    }

    @Override
    public EdmComplexType getComplexType() {
      fail();
      return null;
    }

    @Override
    public EdmComplexType getComplexTypeFilter() {
      fail();
      return null;
    }

  }

  private class UriResourceEntitySetDouble implements UriResourceEntitySet {

    @Override
    public EdmType getType() {
      fail();
      return null;
    }

    @Override
    public boolean isCollection() {
      fail();
      return false;
    }

    @Override
    public String getSegmentValue(final boolean includeFilters) {
      fail();
      return null;
    }

    @Override
    public String toString(final boolean includeFilters) {
      fail();
      return null;
    }

    @Override
    public UriResourceKind getKind() {
      fail();
      return null;
    }

    @Override
    public String getSegmentValue() {
      fail();
      return null;
    }

    @Override
    public EdmEntitySet getEntitySet() {
      fail();
      return null;
    }

    @Override
    public EdmEntityType getEntityType() {
      fail();
      return null;
    }

    @Override
    public List<UriParameter> getKeyPredicates() {
      fail();
      return null;
    }

    @Override
    public EdmType getTypeFilterOnCollection() {
      fail();
      return null;
    }

    @Override
    public EdmType getTypeFilterOnEntry() {
      fail();
      return null;
    }

  }
}
