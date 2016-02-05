package org.apache.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.processor.core.util.EdmEntityTypeDouble;
import org.apache.olingo.jpa.processor.core.util.ExpandItemDouble;
import org.apache.olingo.jpa.processor.core.util.TupleDouble;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.Before;
import org.junit.Test;

public class TestJPAExpandQueryCreateResult extends TestBase {
  private JPAExpandQuery cut;

  @Before
  public void setup() throws ODataJPAModelException, ODataApplicationException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    createHeaders();
    EdmEntityType targetEntity = new EdmEntityTypeDouble(nameBuilder, "BusinessPartnerRole");
    cut = new JPAExpandQuery(helper.sd, emf.createEntityManager(), new ExpandItemDouble(targetEntity).getResourcePath(),
        helper.getJPAAssociationPath("Organizations", "Roles"), null, new HashMap<String, List<String>>());
    // new EdmEntitySetDouble(nameBuilder, "Organisations"), null, new HashMap<String, List<String>>());
  }

  @Test
  public void checkConvertOneResult() throws ODataJPAModelException, ODataApplicationException {
    JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "Roles");
    List<Tuple> result = new ArrayList<Tuple>();
    HashMap<String, Object> oneResult = new HashMap<String, Object>();
    oneResult.put("BusinessPartnerID", "1");
    oneResult.put("RoleCategory", "A");
    Tuple t = new TupleDouble(oneResult);
    result.add(t);

    Map<String, List<Tuple>> act = cut.convertResult(result, exp);

    assertNotNull(act.get("1"));
    assertEquals(1, act.get("1").size());
    assertEquals("1", act.get("1").get(0).get("BusinessPartnerID"));
  }

  @Test
  public void checkConvertTwoResultOneParent() throws ODataJPAModelException, ODataApplicationException {
    JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "Roles");
    List<Tuple> result = new ArrayList<Tuple>();
    HashMap<String, Object> oneResult;
    Tuple t;

    oneResult = new HashMap<String, Object>();
    oneResult.put("BusinessPartnerID", "2");
    oneResult.put("RoleCategory", "A");
    t = new TupleDouble(oneResult);
    result.add(t);
    oneResult = new HashMap<String, Object>();
    oneResult.put("BusinessPartnerID", "2");
    oneResult.put("RoleCategory", "C");
    t = new TupleDouble(oneResult);
    result.add(t);

    Map<String, List<Tuple>> act = cut.convertResult(result, exp);

    assertEquals(1, act.size());
    assertNotNull(act.get("2"));
    assertEquals(2, act.get("2").size());
    assertEquals("2", act.get("2").get(0).get("BusinessPartnerID"));
  }

  @Test
  public void checkConvertTwoResultTwoParent() throws ODataJPAModelException, ODataApplicationException {
    JPAAssociationPath exp = helper.getJPAAssociationPath("Organizations", "Roles");
    List<Tuple> result = new ArrayList<Tuple>();
    HashMap<String, Object> oneResult;
    Tuple t;

    oneResult = new HashMap<String, Object>();
    oneResult.put("BusinessPartnerID", "1");
    oneResult.put("RoleCategory", "A");
    t = new TupleDouble(oneResult);
    result.add(t);
    oneResult = new HashMap<String, Object>();
    oneResult.put("BusinessPartnerID", "2");
    oneResult.put("RoleCategory", "C");
    t = new TupleDouble(oneResult);
    result.add(t);

    Map<String, List<Tuple>> act = cut.convertResult(result, exp);

    assertEquals(2, act.size());
    assertNotNull(act.get("1"));
    assertNotNull(act.get("2"));
    assertEquals(1, act.get("2").size());
    assertEquals("C", act.get("2").get(0).get("RoleCategory"));
  }

  @Test
  public void checkConvertOneResultCompundKey() throws ODataJPAModelException, ODataApplicationException {
    JPAAssociationPath exp = helper.getJPAAssociationPath("AdministrativeDivisions", "Parent");
    List<Tuple> result = new ArrayList<Tuple>();
    HashMap<String, Object> oneResult = new HashMap<String, Object>();
    oneResult.put("CodePublisher", "NUTS");
    oneResult.put("DivisionCode", "BE25");
    oneResult.put("CodeID", "2");
    oneResult.put("ParentCodeID", "1");
    oneResult.put("ParentDivisionCode", "BE2");
    Tuple t = new TupleDouble(oneResult);
    result.add(t);

    Map<String, List<Tuple>> act = cut.convertResult(result, exp);

    assertNotNull(act.get("NUTS/2/BE25"));
    assertEquals(1, act.get("NUTS/2/BE25").size());
    assertEquals("BE2", act.get("NUTS/2/BE25").get(0).get("ParentDivisionCode"));
  }

  @Test
  public void checkConvertTwoResultsCompundKey() throws ODataJPAModelException, ODataApplicationException {
    JPAAssociationPath exp = helper.getJPAAssociationPath("AdministrativeDivisions", "Parent");
    List<Tuple> result = new ArrayList<Tuple>();
    HashMap<String, Object> oneResult;
    Tuple t;

    oneResult = new HashMap<String, Object>();
    oneResult.put("CodePublisher", "NUTS");
    oneResult.put("DivisionCode", "BE25");
    oneResult.put("CodeID", "2");
    oneResult.put("ParentCodeID", "1");
    oneResult.put("ParentDivisionCode", "BE2");
    t = new TupleDouble(oneResult);
    result.add(t);

    oneResult = new HashMap<String, Object>();
    oneResult.put("CodePublisher", "NUTS");
    oneResult.put("DivisionCode", "BE10");
    oneResult.put("CodeID", "2");
    oneResult.put("ParentCodeID", "1");
    oneResult.put("ParentDivisionCode", "BE1");
    t = new TupleDouble(oneResult);
    result.add(t);

    Map<String, List<Tuple>> act = cut.convertResult(result, exp);

    assertEquals(2, act.size());
    assertNotNull(act.get("NUTS/2/BE25"));
    assertEquals(1, act.get("NUTS/2/BE25").size());
    assertEquals("BE2", act.get("NUTS/2/BE25").get(0).get("ParentDivisionCode"));
    assertNotNull(act.get("NUTS/2/BE10"));
    assertEquals(1, act.get("NUTS/2/BE10").size());
    assertEquals("BE1", act.get("NUTS/2/BE10").get(0).get("ParentDivisionCode"));
  }

}
