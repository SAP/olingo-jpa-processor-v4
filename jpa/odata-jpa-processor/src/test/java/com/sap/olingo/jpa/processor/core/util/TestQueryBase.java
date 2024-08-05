package com.sap.olingo.jpa.processor.core.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jakarta.persistence.criteria.Root;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.junit.jupiter.api.BeforeEach;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAODataContextAccessDouble;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.processor.JPAODataInternalRequestContext;
import com.sap.olingo.jpa.processor.core.query.JPAAbstractJoinQuery;
import com.sap.olingo.jpa.processor.core.query.JPAJoinQuery;

public class TestQueryBase extends TestBase {

  protected JPAAbstractJoinQuery cut;
  protected JPAEntityType jpaEntityType;
  protected Root<?> root;
  protected JPAODataSessionContextAccess context;
  protected UriInfo uriInfo;
  protected JPAODataInternalRequestContext requestContext;
  protected JPAODataRequestContext externalContext;
  protected OData odata;

  public TestQueryBase() {
    super();
  }

  @BeforeEach
  public void setup() throws ODataException, ODataJPAIllegalAccessException {
    buildUriInfo("BusinessPartners", "BusinessPartner");
    odata = mock(OData.class);
    helper = new TestHelper(emf, PUNIT_NAME);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    jpaEntityType = helper.getJPAEntityType("BusinessPartners");
    createHeaders();
    context = new JPAODataContextAccessDouble(new JPAEdmProvider(PUNIT_NAME, emf, null, TestBase.enumPackages),
        dataSource, null, null, null);
    externalContext = mock(JPAODataRequestContext.class);
    when(externalContext.getEntityManager()).thenReturn(emf.createEntityManager());
    requestContext = new JPAODataInternalRequestContext(externalContext, context, odata);
    requestContext.setUriInfo(uriInfo);
    cut = new JPAJoinQuery(null, requestContext);

    root = emf.getCriteriaBuilder().createTupleQuery().from(jpaEntityType.getTypeClass());
    joinTables = new HashMap<>();
    joinTables.put(jpaEntityType.getExternalName(), root);
  }

  protected EdmType buildUriInfo(final String esName, final String etName) {
    uriInfo = mock(UriInfo.class);
    final EdmEntitySet odataEs = mock(EdmEntitySet.class);
    final EdmEntityType odataType = mock(EdmEntityType.class);
    final List<UriResource> resources = new ArrayList<>();
    final UriResourceEntitySet esResource = mock(UriResourceEntitySet.class);
    when(uriInfo.getUriResourceParts()).thenReturn(resources);
    when(esResource.getKeyPredicates()).thenReturn(new ArrayList<>(0));
    when(esResource.getEntitySet()).thenReturn(odataEs);
    when(esResource.getKind()).thenReturn(UriResourceKind.entitySet);
    when(esResource.getType()).thenReturn(odataType);
    when(odataEs.getName()).thenReturn(esName);
    when(odataEs.getEntityType()).thenReturn(odataType);
    when(odataType.getNamespace()).thenReturn(PUNIT_NAME);
    when(odataType.getName()).thenReturn(etName);
    resources.add(esResource);
    return odataType;
  }

  protected EdmType buildRequestContext(final String esName, final String etName)
      throws ODataJPAIllegalAccessException {
    final EdmType odataType = buildUriInfo(esName, etName);
    final JPAODataRequestContext externalContext = mock(JPAODataRequestContext.class);
    when(externalContext.getEntityManager()).thenReturn(emf.createEntityManager());
    requestContext = new JPAODataInternalRequestContext(externalContext, context, odata);
    requestContext.setUriInfo(uriInfo);
    return odataType;
  }
}