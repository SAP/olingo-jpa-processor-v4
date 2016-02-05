package org.apache.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.testmodel.Organization;
import org.apache.olingo.jpa.processor.core.util.EdmEntitySetDouble;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.Before;
import org.junit.Test;

public class TestJPAQueryFromClause extends TestBase {
  private JPAExecutableQuery cut;
  private JPAEntityType jpaEntityType;

  @Before
  public void setup() throws ODataJPAModelException, ODataApplicationException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    jpaEntityType = helper.getJPAEntityType("Organizations");
    createHeaders();
    cut = new JPAQuery(new EdmEntitySetDouble(nameBuilder, "Organizations"), helper.sd, null, emf.createEntityManager(),
        headers);
  }

  @Test
  public void checkFromListContainsRoot() throws ODataApplicationException {
    HashMap<String, From<?, ?>> act = cut.createFromClause(new ArrayList<JPAAssociationAttribute>(),
        new ArrayList<JPAPath>());
    assertNotNull(act.get(jpaEntityType.getInternalName()));
  }

  @Test
  public void checkFromListOrderByContainsOne() throws ODataJPAModelException, ODataApplicationException {
    List<JPAAssociationAttribute> orderBy = new ArrayList<JPAAssociationAttribute>();
    JPAAttribute exp = helper.getJPAAssociation("Organizations", "roles");
    orderBy.add((JPAAssociationAttribute) exp);

    HashMap<String, From<?, ?>> act = cut.createFromClause(orderBy, new ArrayList<JPAPath>());
    assertNotNull(act.get(exp.getInternalName()));
  }

  @Test
  public void checkFromListOrderByOuterJoinOne() throws ODataJPAModelException, ODataApplicationException {
    List<JPAAssociationAttribute> orderBy = new ArrayList<JPAAssociationAttribute>();
    JPAAttribute exp = helper.getJPAAssociation("Organizations", "roles");
    orderBy.add((JPAAssociationAttribute) exp);

    HashMap<String, From<?, ?>> act = cut.createFromClause(orderBy, new ArrayList<JPAPath>());

    @SuppressWarnings("unchecked")
    Root<Organization> root = (Root<Organization>) act.get(jpaEntityType.getInternalName());
    Set<Join<Organization, ?>> joins = root.getJoins();
    assertEquals(1, joins.size());

    for (Join<Organization, ?> join : joins) {
      assertEquals(JoinType.LEFT, join.getJoinType());
    }
  }

  @Test
  public void checkFromListOrderByOuterJoinOnConditionOne() throws ODataJPAModelException, ODataApplicationException {
    List<JPAAssociationAttribute> orderBy = new ArrayList<JPAAssociationAttribute>();
    JPAAttribute exp = helper.getJPAAssociation("Organizations", "roles");
    orderBy.add((JPAAssociationAttribute) exp);

    HashMap<String, From<?, ?>> act = cut.createFromClause(orderBy, new ArrayList<JPAPath>());

    @SuppressWarnings("unchecked")
    Root<Organization> root = (Root<Organization>) act.get(jpaEntityType.getInternalName());
    Set<Join<Organization, ?>> joins = root.getJoins();
    assertEquals(1, joins.size());

    for (Join<Organization, ?> join : joins) {
      assertNull(join.getOn());
    }
  }

  @Test
  public void checkFromListDescriptionAssozationAllFields() throws ODataApplicationException, ODataJPAModelException {
    List<JPAAssociationAttribute> orderBy = new ArrayList<JPAAssociationAttribute>();
    List<JPAPath> descriptionPathList = new ArrayList<JPAPath>();
    JPAEntityType entity = helper.getJPAEntityType("Organizations");
    descriptionPathList.add(entity.getPath("Address/CountryName"));

    JPAAttribute attri = helper.getJPAAttribute("Organizations", "address");
    JPAAttribute exp = attri.getStructuredType().getAttribute("countryName");

    HashMap<String, From<?, ?>> act = cut.createFromClause(orderBy, descriptionPathList);
    assertEquals(2, act.size());
    assertNotNull(act.get(exp.getInternalName()));
  }

  @Test
  public void checkFromListDescriptionAssozationAllFields2() throws ODataApplicationException, ODataJPAModelException {
    List<JPAAssociationAttribute> orderBy = new ArrayList<JPAAssociationAttribute>();
    List<JPAPath> descriptionPathList = new ArrayList<JPAPath>();
    JPAEntityType entity = helper.getJPAEntityType("Organizations");
    descriptionPathList.add(entity.getPath("Address/RegionName"));

    JPAAttribute attri = helper.getJPAAttribute("Organizations", "address");
    JPAAttribute exp = attri.getStructuredType().getAttribute("regionName");

    HashMap<String, From<?, ?>> act = cut.createFromClause(orderBy, descriptionPathList);
    assertEquals(2, act.size());
    assertNotNull(act.get(exp.getInternalName()));
  }
}
