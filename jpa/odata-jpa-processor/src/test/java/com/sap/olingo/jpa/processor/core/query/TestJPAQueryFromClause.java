package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
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
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataContextAccessDouble;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.processor.JPAODataInternalRequestContext;
import com.sap.olingo.jpa.processor.core.properties.JPAProcessorAttribute;
import com.sap.olingo.jpa.processor.core.properties.JPAProcessorSimpleAttribute;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

class TestJPAQueryFromClause extends TestBase {
  private JPAAbstractJoinQuery cut;
  private JPAEntityType jpaEntityType;
  private JPAODataSessionContextAccess sessionContext;
  private OData odata;

  @BeforeEach
  void setup() throws ODataException, ODataJPAIllegalAccessException {
    odata = mock(OData.class);
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
        dataSource, null, null, null);
    createHeaders();
    final JPAODataRequestContext externalContext = mock(JPAODataRequestContext.class);
    when(externalContext.getEntityManager()).thenReturn(emf.createEntityManager());
    when(externalContext.getRequestParameter()).thenReturn(mock(JPARequestParameterMap.class));
    final JPAODataInternalRequestContext requestContext = new JPAODataInternalRequestContext(externalContext,
        sessionContext, odata);
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
  void checkFromListOrderByContainsOne() throws ODataApplicationException,
      JPANoSelectionException {
    final List<JPAProcessorAttribute> orderBy = new ArrayList<>();
    final JPAProcessorAttribute exp = buildRoleAssociationPath(orderBy);

    final Map<String, From<?, ?>> act = cut.createFromClause(orderBy, new ArrayList<>(), cut.cq, null);
    assertNotNull(act.get(exp.getAlias()));
  }

  @Test
  void checkFromListDescriptionAssociationAllFields() throws ODataApplicationException, ODataJPAModelException,
      JPANoSelectionException {
    final List<JPAProcessorAttribute> orderBy = new ArrayList<>();
    final List<JPAPath> descriptionPathList = new ArrayList<>();
    final JPAEntityType entity = helper.getJPAEntityType("Organizations");
    descriptionPathList.add(entity.getPath("Address/CountryName"));

    final Map<String, From<?, ?>> act = cut.createFromClause(orderBy, descriptionPathList, cut.cq, null);
    assertEquals(2, act.size());
    assertNotNull(act.get("Address/CountryName"));
  }

  @Test
  void checkFromListDescriptionAssociationAllFields2() throws ODataApplicationException, ODataJPAModelException,
      JPANoSelectionException {
    final List<JPAProcessorAttribute> orderBy = new ArrayList<>();
    final List<JPAPath> descriptionPathList = new ArrayList<>();
    final JPAEntityType entity = helper.getJPAEntityType("Organizations");
    descriptionPathList.add(entity.getPath("Address/RegionName"));

    final Map<String, From<?, ?>> act = cut.createFromClause(orderBy, descriptionPathList, cut.cq, null);
    assertEquals(2, act.size());
    assertNotNull(act.get("Address/RegionName"));
  }

  @Test
  void checkThrowsIfEliminatedByGroups() throws ODataJPAIllegalAccessException, ODataException {

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
        sessionContext, odata);
    requestContext.setUriInfo(uriInfo);
    return requestContext;
  }

  @SuppressWarnings("unchecked")
  private JPAProcessorAttribute buildRoleAssociationPath(final List<JPAProcessorAttribute> orderBy) {

    final var attribute = mock(JPAProcessorSimpleAttribute.class);
    final var join = mock(Join.class);
    when(attribute.requiresJoin()).thenReturn(true);
    when(attribute.getAlias()).thenReturn("Roles");
    when(attribute.createJoin()).thenReturn(join);
    when(attribute.setTarget(any(), any(), any())).thenReturn(attribute);
    orderBy.add(attribute);
    return attribute;
  }

}
