package com.sap.olingo.jpa.processor.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAODataContextAccessDouble;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.JPAIllicalAccessException;
import com.sap.olingo.jpa.processor.core.processor.JPAODataRequestContextImpl;
import com.sap.olingo.jpa.processor.core.query.JPAAbstractJoinQuery;
import com.sap.olingo.jpa.processor.core.query.JPAJoinQuery;

public class TestGroupBase extends TestBase {

  protected JPAAbstractJoinQuery cut;
  protected JPAEntityType jpaEntityType;
  protected HashMap<String, From<?, ?>> joinTables;
  protected Root<?> root;
  protected JPAODataSessionContextAccess context;
  protected UriInfo uriInfo;
  protected JPAODataRequestContextImpl requestContext;

  public TestGroupBase() {
    super();
  }

  @BeforeEach
  public void setup() throws ODataException, JPAIllicalAccessException {
    uriInfo = buildUriInfo("BusinessPartnerWithGroupss", "BusinessPartnerWithGroups");

    helper = new TestHelper(emf, PUNIT_NAME);
    nameBuilder = new JPAEdmNameBuilder(PUNIT_NAME);
    jpaEntityType = helper.getJPAEntityType("BusinessPartnerWithGroupss");
    createHeaders();
    context = new JPAODataContextAccessDouble(new JPAEdmProvider(PUNIT_NAME, emf, null, TestBase.enumPackages), ds,
        null);

    requestContext = new JPAODataRequestContextImpl();
    requestContext.setEntityManager(emf.createEntityManager());
    requestContext.setUriInfo(uriInfo);
    cut = new JPAJoinQuery(null, context, headers, requestContext);

    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables = new HashMap<>();
    joinTables.put(jpaEntityType.getExternalName(), root);
  }

  protected UriInfo buildUriInfo(final String esName, final String etName) {
    final UriInfo ui = Mockito.mock(UriInfo.class);
    final EdmEntitySet odataEs = Mockito.mock(EdmEntitySet.class);
    final EdmType odataType = Mockito.mock(EdmEntityType.class);
    final List<UriResource> resources = new ArrayList<>();
    final UriResourceEntitySet esResource = Mockito.mock(UriResourceEntitySet.class);
    Mockito.when(ui.getUriResourceParts()).thenReturn(resources);
    Mockito.when(esResource.getKeyPredicates()).thenReturn(new ArrayList<>(0));
    Mockito.when(esResource.getEntitySet()).thenReturn(odataEs);
    Mockito.when(esResource.getKind()).thenReturn(UriResourceKind.entitySet);
    Mockito.when(esResource.getType()).thenReturn(odataType);
    Mockito.when(odataEs.getName()).thenReturn(esName);
    Mockito.when(odataType.getNamespace()).thenReturn(PUNIT_NAME);
    Mockito.when(odataType.getName()).thenReturn(etName);
    resources.add(esResource);
    return ui;
  }

//  protected EdmType buildRequestContext(final String esName, final String etName) throws JPAIllicalAccessException {
//    final EdmType odataType = buildUriInfo(esName, etName);
//    requestContext = new JPAODataRequestContextImpl();
//    requestContext.setEntityManager(emf.createEntityManager());
//    requestContext.setUriInfo(uriInfo);
//    return odataType;
//  }

  protected void fillJoinTable(Root<?> joinRoot) {
    Join<?, ?> join = joinRoot.join("locationName", JoinType.LEFT);
    joinTables.put("locationName", join);
    join = joinRoot.join("address", JoinType.LEFT);
    join = join.join("countryName", JoinType.LEFT);
    joinTables.put("countryName", join);
    join = joinRoot.join("address", JoinType.LEFT);
    join = join.join("regionName", JoinType.LEFT);
    joinTables.put("regionName", join);
  }
}