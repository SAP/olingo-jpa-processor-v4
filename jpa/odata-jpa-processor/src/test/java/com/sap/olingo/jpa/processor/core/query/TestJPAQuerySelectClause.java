package com.sap.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceValue;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAODataContextAccessDouble;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.testmodel.TestDataConstants;
import com.sap.olingo.jpa.processor.core.util.EdmEntityTypeDouble;
import com.sap.olingo.jpa.processor.core.util.EdmPropertyDouble;
import com.sap.olingo.jpa.processor.core.util.ExpandItemDouble;
import com.sap.olingo.jpa.processor.core.util.ExpandOptionDouble;
import com.sap.olingo.jpa.processor.core.util.SelectOptionDouble;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;
import com.sap.olingo.jpa.processor.core.util.UriInfoDouble;
import com.sap.olingo.jpa.processor.core.util.UriResourceNavigationDouble;
import com.sap.olingo.jpa.processor.core.util.UriResourcePropertyDouble;

public class TestJPAQuerySelectClause extends TestBase {

  private JPAAbstractJoinQuery cut;
  private JPAEntityType jpaEntityType;
  private HashMap<String, From<?, ?>> joinTables;
  private Root<?> root;
  private JPAODataSessionContextAccess context;
  private UriInfo uriInfo;

  @Before
  public void setup() throws ODataException {
    buildUriInfo("BusinessPartners", "BusinessPartner");

    helper = new TestHelper(emf, PUNIT_NAME);
    nameBuilder = new JPAEdmNameBuilder(PUNIT_NAME);
    jpaEntityType = helper.getJPAEntityType("BusinessPartners");
    createHeaders();
    context = new JPAODataContextAccessDouble(new JPAEdmProvider(PUNIT_NAME, emf, null, TestBase.enumPackages), ds,
        null);

    cut = new JPAJoinQuery(null, context, emf.createEntityManager(), headers, uriInfo);

    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables = new HashMap<>();
    joinTables.put(jpaEntityType.getInternalName(), root);
  }

  private void buildUriInfo(final String esName, final String etName) {
    uriInfo = Mockito.mock(UriInfo.class);
    final EdmEntitySet odataEs = Mockito.mock(EdmEntitySet.class);
    final EdmType odataType = Mockito.mock(EdmEntityType.class);
    final List<UriResource> resources = new ArrayList<>();
    final UriResourceEntitySet esResource = Mockito.mock(UriResourceEntitySet.class);
    Mockito.when(uriInfo.getUriResourceParts()).thenReturn(resources);
    Mockito.when(esResource.getKeyPredicates()).thenReturn(new ArrayList<>(1));
    Mockito.when(esResource.getEntitySet()).thenReturn(odataEs);
    Mockito.when(esResource.getKind()).thenReturn(UriResourceKind.entitySet);
    Mockito.when(esResource.getType()).thenReturn(odataType);
    Mockito.when(odataEs.getName()).thenReturn(esName);
    Mockito.when(odataType.getNamespace()).thenReturn(PUNIT_NAME);
    Mockito.when(odataType.getName()).thenReturn(etName);
    resources.add(esResource);
  }

  @Test
  public void checkSelectAll() throws ODataApplicationException, ODataJPAModelException {
    fillJoinTable(root);

    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("*"))), root);
    assertEquals(jpaEntityType.getPathList().size(), selectClause.size());

  }

  @Test
  public void checkSelectAllWithSelectionNull() throws ODataApplicationException, ODataJPAModelException {
    fillJoinTable(root);

    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(new UriInfoDouble(
        null)), root);

    assertEquals(jpaEntityType.getPathList().size(), selectClause.size());
  }

  @Test
  public void checkSelectExpandViaIgnoredProperties() throws ODataApplicationException, ODataJPAModelException {
    // Organizations('3')/Address?$expand=AdministrativeDivision
    fillJoinTable(root);
    List<ExpandItem> expItems = new ArrayList<>();
    EdmEntityType startEntity = new EdmEntityTypeDouble(nameBuilder, "Organization");
    EdmEntityType targetEntity = new EdmEntityTypeDouble(nameBuilder, "AdministrativeDivision");

    ExpandOption expOps = new ExpandOptionDouble("AdministrativeDivision", expItems);
    expItems.add(new ExpandItemDouble(targetEntity));
    List<UriResource> startResources = new ArrayList<>();
    UriInfoDouble uriInfo = new UriInfoDouble(null);
    uriInfo.setExpandOpts(expOps);
    uriInfo.setUriResources(startResources);

    startResources.add(new UriResourceNavigationDouble(startEntity));
    startResources.add(new UriResourcePropertyDouble(new EdmPropertyDouble("Address")));

    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(uriInfo), root);

    assertContains(selectClause, "Address/RegionCodeID");
  }

  private void fillJoinTable(Root<?> joinRoot) {
    Join<?, ?> join = joinRoot.join("locationName", JoinType.LEFT);
    joinTables.put("locationName", join);
    join = joinRoot.join("address", JoinType.LEFT);
    join = join.join("countryName", JoinType.LEFT);
    joinTables.put("countryName", join);
    join = joinRoot.join("address", JoinType.LEFT);
    join = join.join("regionName", JoinType.LEFT);
    joinTables.put("regionName", join);
  }

  @Test
  public void checkSelectOnePropertyCreatedAt() throws ODataApplicationException, ODataJPAModelException {
    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("CreationDateTime"))), root);
    assertEquals(2, selectClause.size());
    assertContains(selectClause, "CreationDateTime");
    assertContains(selectClause, "ID");
  }

  @Test
  public void checkSelectOnePropertyID() throws ODataApplicationException, ODataJPAModelException {
    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("ID"))), root);
    assertEquals(1, selectClause.size());
    assertContains(selectClause, "ID");
  }

  @Test
  public void checkSelectOnePropertyPartKey() throws ODataException {
    jpaEntityType = helper.getJPAEntityType("AdministrativeDivisionDescriptions");
    buildUriInfo("AdministrativeDivisionDescriptions", "AdministrativeDivisionDescription");

    cut = new JPAJoinQuery(null, context, emf.createEntityManager(), headers, uriInfo);

    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);

    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble((new SelectOptionDouble("CodePublisher")))), root);
    assertEquals(4, selectClause.size());
    assertContains(selectClause, "CodePublisher");
    assertContains(selectClause, "CodeID");
    assertContains(selectClause, "DivisionCode");
    assertContains(selectClause, "Language");
  }

  @Test
  public void checkSelectPropertyTypeCreatedAt() throws ODataApplicationException, ODataJPAModelException {
    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Type,CreationDateTime"))), root);

    assertEquals(3, selectClause.size());
    assertContains(selectClause, "CreationDateTime");
    assertContains(selectClause, "Type");
    assertContains(selectClause, "ID");
  }

  @Test
  public void checkSelectSupertypePropertyTypeName2() throws ODataException {
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);
    buildUriInfo("Organizations", "Organization");

    cut = new JPAJoinQuery(null, context, emf.createEntityManager(), headers, uriInfo);

    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Type,Name2"))), root);
    assertContains(selectClause, "Name2");
    assertContains(selectClause, "Type");
    assertContains(selectClause, "ID");
    assertEquals(3, selectClause.size());
  }

  @Test
  public void checkSelectCompleteComplexType() throws ODataException {
    // Organizations$select=Address
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);
    fillJoinTable(root);

    cut = new JPAJoinQuery(null, context, emf.createEntityManager(), headers, uriInfo);

    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Address"))), root);
    assertEquals(TestDataConstants.NO_ATTRIBUTES_POSTAL_ADDRESS + 1, selectClause.size());
  }

  @Test
  public void checkSelectCompleteNestedComplexTypeLowLevel() throws ODataException {
    // Organizations$select=Address
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);

    cut = new JPAJoinQuery(null, context, emf.createEntityManager(), headers, uriInfo);

    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("AdministrativeInformation/Created"))), root);
    assertEquals(3, selectClause.size());
    assertContains(selectClause, "AdministrativeInformation/Created/By");
    assertContains(selectClause, "AdministrativeInformation/Created/At");
    assertContains(selectClause, "ID");
  }

  @Test
  public void checkSelectCompleteNestedComplexTypeHighLevel() throws ODataException {
    // Organizations$select=Address
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);

    cut = new JPAJoinQuery(null, context, emf.createEntityManager(), headers, uriInfo);

    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("AdministrativeInformation"))), root);
    assertEquals(5, selectClause.size());
    assertContains(selectClause, "AdministrativeInformation/Created/By");
    assertContains(selectClause, "AdministrativeInformation/Created/At");
    assertContains(selectClause, "AdministrativeInformation/Updated/By");
    assertContains(selectClause, "AdministrativeInformation/Updated/At");
    assertContains(selectClause, "ID");
  }

  @Test
  public void checkSelectElementOfComplexType() throws ODataException {
    // Organizations$select=Address/Country
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables.put(jpaEntityType.getInternalName(), root);

    cut = new JPAJoinQuery(null, context, emf.createEntityManager(), headers, uriInfo);

    // SELECT c.address.geocode FROM Company c WHERE c.name = 'Random House'
    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Address/Country"))), root);
    assertContains(selectClause, "Address/Country");
    assertContains(selectClause, "ID");
    assertEquals(2, selectClause.size());
  }

  @Test
  public void checkSelectTextJoinSingleAttribute() throws ODataException {
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    cut = new JPAJoinQuery(null, context, emf.createEntityManager(), headers, uriInfo);
    joinTables.put(jpaEntityType.getInternalName(), root);
    fillJoinTable(root);

    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Address/CountryName"))), root);
    assertContains(selectClause, "Address/CountryName");
    assertContains(selectClause, "ID");
    assertEquals(2, selectClause.size());
  }

  @Test
  public void checkSelectTextJoinCompextType() throws ODataException {
    jpaEntityType = helper.getJPAEntityType("Organizations");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    cut = new JPAJoinQuery(null, context, emf.createEntityManager(), headers, uriInfo);
    joinTables.put(jpaEntityType.getInternalName(), root);
    fillJoinTable(root);

    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(
        new UriInfoDouble(new SelectOptionDouble("Address"))), root);
    assertEquals(TestDataConstants.NO_ATTRIBUTES_POSTAL_ADDRESS + 1, selectClause.size());
    assertContains(selectClause, "Address/CountryName");
    assertContains(selectClause, "ID");
  }

  @Test
  public void checkSelectStreamValueStatic() throws ODataException {
    jpaEntityType = helper.getJPAEntityType("PersonImages");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    buildUriInfo("PersonImages", "PersonImage");

    cut = new JPAJoinQuery(null, context, emf.createEntityManager(), headers, uriInfo);

    UriInfoDouble uriInfo = new UriInfoDouble(new SelectOptionDouble("Address"));
    List<UriResource> uriResources = new ArrayList<>();
    uriInfo.setUriResources(uriResources);
    uriResources.add(new UriResourceEntitySetDouble());
    uriResources.add(new UriResourceValueDouble());

    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(uriInfo), root);
    assertNotNull(selectClause);
    assertContains(selectClause, "Image");
    assertContains(selectClause, "ID");
  }

  @Test
  public void checkSelectStreamValueDynamic() throws ODataException {
    jpaEntityType = helper.getJPAEntityType("OrganizationImages");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    buildUriInfo("OrganizationImages", "OrganizationImage");

    cut = new JPAJoinQuery(null, context, emf.createEntityManager(), headers, uriInfo);

    UriInfoDouble uriInfo = new UriInfoDouble(new SelectOptionDouble("Address"));
    List<UriResource> uriResources = new ArrayList<>();
    uriInfo.setUriResources(uriResources);
    uriResources.add(new UriResourceEntitySetDouble());
    uriResources.add(new UriResourceValueDouble());

    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(uriInfo), root);
    assertNotNull(selectClause);
    assertContains(selectClause, "Image");
    assertContains(selectClause, "MimeType");
    assertContains(selectClause, "ID");
  }

  @Test
  public void checkSelectPropertyValue() throws ODataException {
    jpaEntityType = helper.getJPAEntityType("PersonImages");
    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    buildUriInfo("PersonImages", "PersonImage");
    cut = new JPAJoinQuery(null, context, emf.createEntityManager(), headers, uriInfo);

    UriInfoDouble uriInfo = new UriInfoDouble(null);
    List<UriResource> uriResources = new ArrayList<>();
    uriInfo.setUriResources(uriResources);
    // PersonImages('99')/AdministrativeInformation/Created/By/$value
    uriResources.add(new UriResourceEntitySetDouble());
    uriResources.add(new UriResourceComplexPropertyDouble(new EdmPropertyDouble("AdministrativeInformation")));
    uriResources.add(new UriResourceComplexPropertyDouble(new EdmPropertyDouble("Created")));
    uriResources.add(new UriResourcePropertyDouble(new EdmPropertyDouble("By")));
    uriResources.add(new UriResourceValueDouble());

    List<Selection<?>> selectClause = cut.createSelectClause(joinTables, cut.buildSelectionPathList(uriInfo), root);
    assertNotNull(selectClause);
    assertContains(selectClause, "AdministrativeInformation/Created/By");
    assertContains(selectClause, "ID");
  }

  private void assertContains(List<Selection<?>> selectClause, String alias) {
    for (Selection<?> selection : selectClause) {
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

    public UriResourceComplexPropertyDouble(EdmProperty property) {
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
    public String getSegmentValue(boolean includeFilters) {
      fail();
      return null;
    }

    @Override
    public String toString(boolean includeFilters) {
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
    public String getSegmentValue(boolean includeFilters) {
      fail();
      return null;
    }

    @Override
    public String toString(boolean includeFilters) {
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
