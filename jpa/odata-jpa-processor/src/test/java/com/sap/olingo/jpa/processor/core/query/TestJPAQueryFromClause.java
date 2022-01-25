package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataContextAccessDouble;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.processor.JPAODataInternalRequestContext;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

class TestJPAQueryFromClause extends TestBase {
  private JPAAbstractJoinQuery cut;
  private JPAEntityType jpaEntityType;
  private JPAODataSessionContextAccess sessionContext;

  @BeforeEach
  void setup() throws ODataException, ODataJPAIllegalAccessException {
    final UriInfo uriInfo = mock(UriInfo.class);
    final EdmEntitySet odataEs = mock(EdmEntitySet.class);
    final EdmEntityType odataType = mock(EdmEntityType.class);
    final List<UriResource> resources = new ArrayList<>();
    final UriResourceEntitySet esResource = mock(UriResourceEntitySet.class);
    when(uriInfo.getUriResourceParts()).thenReturn(resources);
    when(esResource.getKeyPredicates()).thenReturn(new ArrayList<>(0));
    when(esResource.getEntitySet()).thenReturn(odataEs);
    when(esResource.getKind()).thenReturn(UriResourceKind.entitySet);
    when(esResource.getType()).thenReturn(odataType);
    when(odataEs.getName()).thenReturn("Organizations");
    when(odataEs.getEntityType()).thenReturn(odataType);
    when(odataType.getNamespace()).thenReturn(PUNIT_NAME);
    when(odataType.getName()).thenReturn("Organization");
    resources.add(esResource);

    helper = new TestHelper(emf, PUNIT_NAME);
    jpaEntityType = helper.getJPAEntityType("Organizations");
    sessionContext = new JPAODataContextAccessDouble(new JPAEdmProvider(PUNIT_NAME, emf, null, TestBase.enumPackages),
        ds, null);
    createHeaders();
    final JPAODataRequestContext externalContext = mock(JPAODataRequestContext.class);
    when(externalContext.getEntityManager()).thenReturn(emf.createEntityManager());
    when(externalContext.getRequestParameter()).thenReturn(mock(JPARequestParameterMap.class));
    final JPAODataInternalRequestContext requestContext = new JPAODataInternalRequestContext(externalContext,
        sessionContext);
    requestContext.setUriInfo(uriInfo);
    cut = new JPAJoinQuery(null, requestContext);
  }

  @Test
  void checkFromListContainsRoot() throws ODataApplicationException, JPANoSelectionException {

    final Map<String, From<?, ?>> act = cut.createFromClause(Collections.emptyList(), Collections.emptyList(), cut.cq,
        null);
    assertNotNull(act.get(jpaEntityType.getExternalFQN().getFullQualifiedNameAsString()));
  }

  @Test
  void checkFromListOrderByContainsOne() throws ODataJPAModelException, ODataApplicationException,
      JPANoSelectionException {
    final List<JPAAssociationPath> orderBy = new ArrayList<>();
    final JPAAssociationPath exp = buildRoleAssociationPath(orderBy);

    final Map<String, From<?, ?>> act = cut.createFromClause(orderBy, new ArrayList<JPAPath>(), cut.cq, null);
    assertNotNull(act.get(exp.getAlias()));
  }

  @Test
  void checkFromListOrderByOuterJoinOne() throws ODataJPAModelException, ODataApplicationException,
      JPANoSelectionException {
    final List<JPAAssociationPath> orderBy = new ArrayList<>();
    buildRoleAssociationPath(orderBy);

    final Map<String, From<?, ?>> act = cut.createFromClause(orderBy, new ArrayList<>(), cut.cq, null);

    @SuppressWarnings("unchecked")
    final Root<Organization> root = (Root<Organization>) act.get(jpaEntityType.getExternalFQN()
        .getFullQualifiedNameAsString());
    final Set<Join<Organization, ?>> joins = root.getJoins();
    assertEquals(1, joins.size());

    for (final Join<Organization, ?> join : joins) {
      assertEquals(JoinType.LEFT, join.getJoinType());
    }
  }

  @Test
  void checkFromListOrderByOuterJoinOnConditionOne() throws ODataJPAModelException, ODataApplicationException,
      JPANoSelectionException {
    final List<JPAAssociationPath> orderBy = new ArrayList<>();
    buildRoleAssociationPath(orderBy);

    final Map<String, From<?, ?>> act = cut.createFromClause(orderBy, new ArrayList<>(), cut.cq, null);

    @SuppressWarnings("unchecked")
    final Root<Organization> root = (Root<Organization>) act.get(jpaEntityType.getExternalFQN()
        .getFullQualifiedNameAsString());
    final Set<Join<Organization, ?>> joins = root.getJoins();
    assertEquals(1, joins.size());

    for (final Join<Organization, ?> join : joins) {
      assertNull(join.getOn());
    }
  }

  @Test
  void checkFromListDescriptionAssozationAllFields() throws ODataApplicationException, ODataJPAModelException,
      JPANoSelectionException {
    final List<JPAAssociationPath> orderBy = new ArrayList<>();
    final List<JPAPath> descriptionPathList = new ArrayList<>();
    final JPAEntityType entity = helper.getJPAEntityType("Organizations");
    descriptionPathList.add(entity.getPath("Address/CountryName"));

    final JPAAttribute attri = helper.getJPAAttribute("Organizations", "address").get();
    final JPAAttribute exp = attri.getStructuredType().getAttribute("countryName").get();

    final Map<String, From<?, ?>> act = cut.createFromClause(orderBy, descriptionPathList, cut.cq, null);
    assertEquals(2, act.size());
    assertNotNull(act.get(exp.getInternalName()));
  }

  @Test
  void checkFromListDescriptionAssozationAllFields2() throws ODataApplicationException, ODataJPAModelException,
      JPANoSelectionException {
    final List<JPAAssociationPath> orderBy = new ArrayList<>();
    final List<JPAPath> descriptionPathList = new ArrayList<>();
    final JPAEntityType entity = helper.getJPAEntityType("Organizations");
    descriptionPathList.add(entity.getPath("Address/RegionName"));

    final JPAAttribute attri = helper.getJPAAttribute("Organizations", "address").get();
    final JPAAttribute exp = attri.getStructuredType().getAttribute("regionName").get();

    final Map<String, From<?, ?>> act = cut.createFromClause(orderBy, descriptionPathList, cut.cq, null);
    assertEquals(2, act.size());
    assertNotNull(act.get(exp.getInternalName()));
  }

  @Test
  void checkThrowsIfEliminatedByGroups() throws ODataJPAIllegalAccessException, ODataException,
      JPANoSelectionException {

    final JPAODataInternalRequestContext requestContext = buildRequestContextToTestGroups(null);

    final List<JPAPath> collectionPathList = new ArrayList<>();
    final JPAEntityType entity = helper.getJPAEntityType("BusinessPartnerWithGroupss");
    collectionPathList.add(entity.getPath("ID"));
    collectionPathList.add(entity.getPath("Comment"));

    cut = new JPAJoinQuery(null, requestContext);

    assertThrows(JPANoSelectionException.class,
        () -> cut.createFromClause(Collections.emptyList(), collectionPathList, cut.cq, cut.lastInfo));

  }

  @Test
  void checkDoesNotThrowsIfGroupProvided() throws ODataJPAIllegalAccessException, ODataException,
      JPANoSelectionException {
    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    final JPAODataInternalRequestContext requestContext = buildRequestContextToTestGroups(groups);
    groups.addGroup("Company");
    final List<JPAPath> collectionPathList = new ArrayList<>();
    final JPAEntityType entity = helper.getJPAEntityType("BusinessPartnerWithGroupss");
    collectionPathList.add(entity.getPath("ID"));
    collectionPathList.add(entity.getPath("Comment"));

    cut = new JPAJoinQuery(null, requestContext);

    final Map<String, From<?, ?>> act = cut.createFromClause(Collections.emptyList(), collectionPathList, cut.cq,
        cut.lastInfo);
    assertEquals(2, act.size());

  }

  private JPAODataInternalRequestContext buildRequestContextToTestGroups(final JPAODataGroupsProvider groups)
      throws ODataJPAIllegalAccessException {
    final UriInfo uriInfo = mock(UriInfo.class);
    final EdmEntitySet odataEs = mock(EdmEntitySet.class);
    final EdmEntityType odataType = mock(EdmEntityType.class);
    final List<UriResource> resources = new ArrayList<>();
    final UriResourceEntitySet esResource = mock(UriResourceEntitySet.class);
    final UriResourcePrimitiveProperty ppResource = mock(UriResourcePrimitiveProperty.class);
    final EdmProperty ppProperty = mock(EdmProperty.class);
    when(uriInfo.getUriResourceParts()).thenReturn(resources);
    when(esResource.getKeyPredicates()).thenReturn(new ArrayList<>(0));
    when(esResource.getEntitySet()).thenReturn(odataEs);
    when(esResource.getKind()).thenReturn(UriResourceKind.entitySet);
    when(esResource.getType()).thenReturn(odataType);
    when(odataEs.getName()).thenReturn("BusinessPartnerWithGroupss");
    when(odataEs.getEntityType()).thenReturn(odataType);
    when(odataType.getNamespace()).thenReturn(PUNIT_NAME);
    when(odataType.getName()).thenReturn("BusinessPartnerWithGroups");
    when(ppResource.isCollection()).thenReturn(true);
    when(ppResource.getProperty()).thenReturn(ppProperty);
    when(ppProperty.getName()).thenReturn("Comment");
    resources.add(esResource);
    resources.add(ppResource);

    final JPAODataRequestContext externalContext = mock(JPAODataRequestContext.class);
    when(externalContext.getEntityManager()).thenReturn(emf.createEntityManager());
    when(externalContext.getGroupsProvider()).thenReturn(Optional.ofNullable(groups));
    when(externalContext.getRequestParameter()).thenReturn(mock(JPARequestParameterMap.class));
    final JPAODataInternalRequestContext requestContext = new JPAODataInternalRequestContext(externalContext,
        sessionContext);
    requestContext.setUriInfo(uriInfo);
    return requestContext;
  }

  private JPAAssociationPath buildRoleAssociationPath(final List<JPAAssociationPath> orderBy)
      throws ODataJPAModelException {
    final JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "Roles");
    orderBy.add(exp);
    return exp;
  }

}
