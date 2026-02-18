package com.sap.olingo.jpa.processor.core.modify;

import static com.sap.olingo.jpa.processor.core.converter.JPAExpandResult.ROOT_RESULT_KEY;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Tuple;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataApiVersionAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.processor.JPAODataInternalRequestContext;
import com.sap.olingo.jpa.processor.core.query.JPAExpandQueryResult;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.util.ServiceMetadataDouble;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;
import com.sap.olingo.jpa.processor.core.util.TupleDouble;
import com.sap.olingo.jpa.processor.core.util.UriHelperDouble;

class JPAEntityNavigationLinkResultTest extends TestBase {
  private JPATupleChildConverter childConverter;
  private List<Tuple> jpaCreateResult;
  private UriHelperDouble uriHelper;
  private Map<String, String> keyPredicates;
  private final Map<String, List<Tuple>> createResult = new HashMap<>(1);
  private JPAODataRequestContextAccess requestContext;
  private JPAODataRequestContext context;
  private JPAODataSessionContextAccess sessionContext;
  private OData odata;
  private JPAODataApiVersionAccess version;

  @BeforeEach
  void setup() throws ODataException {
    helper = new TestHelper(emf, PUNIT_NAME);
    createHeaders();
    jpaCreateResult = new ArrayList<>();
    createResult.put(ROOT_RESULT_KEY, jpaCreateResult);
    uriHelper = new UriHelperDouble();
    keyPredicates = new HashMap<>();
    uriHelper.setKeyPredicates(keyPredicates, "ID");
    version = mock(JPAODataApiVersionAccess.class);

    context = mock(JPAODataRequestContext.class);
    sessionContext = mock(JPAODataSessionContextAccess.class);
    when(version.getEntityManagerFactory()).thenReturn(emf);
    when(sessionContext.getApiVersion(any())).thenReturn(version);
    odata = mock(OData.class);
    requestContext = new JPAODataInternalRequestContext(context, sessionContext, odata);
    childConverter = new JPATupleChildConverter(helper.sd, uriHelper, new ServiceMetadataDouble(nameBuilder,
        "Organization"), requestContext);
  }

  @Test
  void checkConvertsOneResultOneKeyWithLink() throws ODataApplicationException, ODataJPAModelException {
    final var et = helper.getJPAEntityType(Organization.class);
    final Map<String, Object> result = new HashMap<>();
    keyPredicates.put("1", "'1'");
    result.put("ID", "1");
    jpaCreateResult.add(new TupleDouble(result));

    final var jpaResult = new JPAExpandQueryResult(createResult, null, et, List.of(), empty());
    final var linkResult = new JPAEntityNavigationLinkResult(et, List.of(new BusinessPartnerRole("1", "2")), headers,
        childConverter, "1");

    final Map<JPAAssociationPath, JPAExpandResult> childResults = Map.of(et.getAssociationPath("Roles"), linkResult);
    jpaResult.putChildren(childResults);

    final var act = childConverter.getResult(jpaResult, List.of()).get(ROOT_RESULT_KEY);
    assertEquals(1, act.getEntities().size());
    assertEquals("Organizations" + "('1')", act.getEntities().get(0).getId().getPath());
  }

}
