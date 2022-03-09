package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataContextAccessDouble;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.processor.JPAODataInternalRequestContext;
import com.sap.olingo.jpa.processor.core.util.EdmEntityTypeDouble;
import com.sap.olingo.jpa.processor.core.util.ExpandItemDouble;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;
import com.sap.olingo.jpa.processor.core.util.TupleDouble;
import com.sap.olingo.jpa.processor.core.util.UriInfoDouble;

class TestJPAExpandQueryCreateResult extends TestBase {
  private JPAExpandJoinQuery cut;
  private JPAODataSessionContextAccess sessionContext;
  private JPAODataInternalRequestContext requestContext;

  @BeforeEach
  void setup() throws ODataException, ODataJPAIllegalAccessException {
    helper = new TestHelper(emf, PUNIT_NAME);
    createHeaders();
    final EdmEntityType targetEntity = new EdmEntityTypeDouble(nameBuilder, "BusinessPartnerRole");
    sessionContext = new JPAODataContextAccessDouble(new JPAEdmProvider(PUNIT_NAME, emf, null,
        TestBase.enumPackages), ds, null);

    final JPAODataRequestContext externalContext = mock(JPAODataRequestContext.class);
    when(externalContext.getEntityManager()).thenReturn(emf.createEntityManager());
    requestContext = new JPAODataInternalRequestContext(externalContext, sessionContext);
    requestContext.setUriInfo(new UriInfoDouble(new ExpandItemDouble(targetEntity).getResourcePath()));

    cut = new JPAExpandJoinQuery(null, helper.getJPAAssociationPath("Organizations", "Roles"),
        helper.sd.getEntity(targetEntity), requestContext);
  }

  @Test
  void checkConvertOneResult() throws ODataJPAModelException, ODataApplicationException {
    final JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "Roles");
    final List<Tuple> result = new ArrayList<>();
    final HashMap<String, Object> oneResult = new HashMap<>();
    oneResult.put("BusinessPartnerID", "1");
    oneResult.put("RoleCategory", "A");
    final Tuple t = new TupleDouble(oneResult);
    result.add(t);

    final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 0, Long.MAX_VALUE);

    assertNotNull(act.get("1"));
    assertEquals(1, act.get("1").size());
    assertEquals("1", act.get("1").get(0).get("BusinessPartnerID"));
  }

  @Test
  void checkConvertTwoResultOneParent() throws ODataJPAModelException, ODataApplicationException {
    final JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "Roles");
    final List<Tuple> result = new ArrayList<>();
    HashMap<String, Object> oneResult;
    Tuple t;

    oneResult = new HashMap<>();
    oneResult.put("BusinessPartnerID", "2");
    oneResult.put("RoleCategory", "A");
    t = new TupleDouble(oneResult);
    result.add(t);
    oneResult = new HashMap<>();
    oneResult.put("BusinessPartnerID", "2");
    oneResult.put("RoleCategory", "C");
    t = new TupleDouble(oneResult);
    result.add(t);

    final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 0, Long.MAX_VALUE);

    assertEquals(1, act.size());
    assertNotNull(act.get("2"));
    assertEquals(2, act.get("2").size());
    assertEquals("2", act.get("2").get(0).get("BusinessPartnerID"));
  }

  @Test
  void checkConvertTwoResultOneParentTop1() throws ODataJPAModelException, ODataApplicationException {
    final JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "Roles");
    final List<Tuple> result = new ArrayList<>();
    HashMap<String, Object> oneResult;
    Tuple t;

    oneResult = new HashMap<>();
    oneResult.put("BusinessPartnerID", "2");
    oneResult.put("RoleCategory", "A");
    t = new TupleDouble(oneResult);
    result.add(t);
    oneResult = new HashMap<>();
    oneResult.put("BusinessPartnerID", "2");
    oneResult.put("RoleCategory", "C");
    t = new TupleDouble(oneResult);
    result.add(t);

    final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 0, 1);

    assertEquals(1, act.size());
    assertNotNull(act.get("2"));
    assertEquals(1, act.get("2").size());
    assertEquals("A", act.get("2").get(0).get("RoleCategory"));
  }

  @Test
  void checkConvertTwoResultOneParentSkip1() throws ODataJPAModelException, ODataApplicationException {
    final JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "Roles");
    final List<Tuple> result = new ArrayList<>();
    HashMap<String, Object> oneResult;
    Tuple t;

    oneResult = new HashMap<>();
    oneResult.put("BusinessPartnerID", "2");
    oneResult.put("RoleCategory", "A");
    t = new TupleDouble(oneResult);
    result.add(t);
    oneResult = new HashMap<>();
    oneResult.put("BusinessPartnerID", "2");
    oneResult.put("RoleCategory", "C");
    t = new TupleDouble(oneResult);
    result.add(t);

    final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 1, 1000);

    assertEquals(1, act.size());
    assertNotNull(act.get("2"));
    assertEquals(1, act.get("2").size());
    assertEquals("C", act.get("2").get(0).get("RoleCategory"));
  }

  @Test
  void checkConvertTwoResultTwoParent() throws ODataJPAModelException, ODataApplicationException {
    final JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "Roles");
    final List<Tuple> result = new ArrayList<>();
    HashMap<String, Object> oneResult;
    Tuple t;

    oneResult = new HashMap<>();
    oneResult.put("BusinessPartnerID", "1");
    oneResult.put("RoleCategory", "A");
    t = new TupleDouble(oneResult);
    result.add(t);
    oneResult = new HashMap<>();
    oneResult.put("BusinessPartnerID", "2");
    oneResult.put("RoleCategory", "C");
    t = new TupleDouble(oneResult);
    result.add(t);

    final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 0, Long.MAX_VALUE);

    assertEquals(2, act.size());
    assertNotNull(act.get("1"));
    assertNotNull(act.get("2"));
    assertEquals(1, act.get("2").size());
    assertEquals("C", act.get("2").get(0).get("RoleCategory"));
  }

  @Test
  void checkConvertOneResultCompoundKey() throws ODataJPAModelException, ODataApplicationException {
    final JPAAssociationPath exp = helper.getJPAAssociationPath("AdministrativeDivisions", "Parent");
    final List<Tuple> result = new ArrayList<>();
    final HashMap<String, Object> oneResult = new HashMap<>();
    oneResult.put("CodePublisher", "NUTS");
    oneResult.put("DivisionCode", "BE25");
    oneResult.put("CodeID", "2");
    oneResult.put("ParentCodeID", "1");
    oneResult.put("ParentDivisionCode", "BE2");
    final Tuple t = new TupleDouble(oneResult);
    result.add(t);

    final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 0, Long.MAX_VALUE);

    assertNotNull(act.get("NUTS/2/BE25"));
    assertEquals(1, act.get("NUTS/2/BE25").size());
    assertEquals("BE2", act.get("NUTS/2/BE25").get(0).get("ParentDivisionCode"));
  }

  @Test
  void checkConvertTwoResultsCompoundKey() throws ODataJPAModelException, ODataApplicationException {
    final JPAAssociationPath exp = helper.getJPAAssociationPath("AdministrativeDivisions", "Parent");
    final List<Tuple> result = new ArrayList<>();
    HashMap<String, Object> oneResult;
    Tuple t;

    oneResult = new HashMap<>();
    oneResult.put("CodePublisher", "NUTS");
    oneResult.put("DivisionCode", "BE25");
    oneResult.put("CodeID", "2");
    oneResult.put("ParentCodeID", "1");
    oneResult.put("ParentDivisionCode", "BE2");
    t = new TupleDouble(oneResult);
    result.add(t);

    oneResult = new HashMap<>();
    oneResult.put("CodePublisher", "NUTS");
    oneResult.put("DivisionCode", "BE10");
    oneResult.put("CodeID", "2");
    oneResult.put("ParentCodeID", "1");
    oneResult.put("ParentDivisionCode", "BE1");
    t = new TupleDouble(oneResult);
    result.add(t);

    final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 0, Long.MAX_VALUE);

    assertEquals(2, act.size());
    assertNotNull(act.get("NUTS/2/BE25"));
    assertEquals(1, act.get("NUTS/2/BE25").size());
    assertEquals("BE2", act.get("NUTS/2/BE25").get(0).get("ParentDivisionCode"));
    assertNotNull(act.get("NUTS/2/BE10"));
    assertEquals(1, act.get("NUTS/2/BE10").size());
    assertEquals("BE1", act.get("NUTS/2/BE10").get(0).get("ParentDivisionCode"));
  }

  @Test
  void checkConvertOneResultJoinTable() throws ODataException {
    final JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "SupportEngineers");

    final EdmEntityType targetEntity = new EdmEntityTypeDouble(nameBuilder, "Person");
    cut = new JPAExpandJoinQuery(null, helper.getJPAAssociationPath("Organizations",
        "SupportEngineers"), helper.sd.getEntity(targetEntity), requestContext);

    final List<Tuple> result = new ArrayList<>();
    final HashMap<String, Object> oneResult = new HashMap<>();
    oneResult.put("SupportEngineers" + JPAExpandJoinQuery.ALIAS_SEPARATOR + "ID", "2");
    oneResult.put("ID", "97");
    final Tuple t = new TupleDouble(oneResult);
    result.add(t);

    final Map<String, List<Tuple>> act = cut.convertResult(result, exp, 0, Long.MAX_VALUE);

    assertNotNull(act.get("2"));
    assertEquals(1, act.get("2").size());
    assertEquals("97", act.get("2").get(0).get("ID"));
  }
}
